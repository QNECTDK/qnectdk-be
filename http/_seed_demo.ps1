$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'

function Call($method, $path, $body, $token) {
  $headers = @{}
  if ($token) { $headers['Authorization'] = "Bearer $token" }
  $p = @{ Method = $method; Uri = "$base$path"; Headers = $headers; SkipHttpErrorCheck = $true }
  if ($null -ne $body) {
    $p['ContentType'] = 'application/json'
    $p['Body'] = ($body | ConvertTo-Json -Depth 12)
  }
  $r = Invoke-WebRequest @p
  $obj = $null
  try { $obj = $r.Content | ConvertFrom-Json } catch {}
  return [pscustomobject]@{ Status = [int]$r.StatusCode; Body = $obj; Raw = $r.Content }
}

function Login($loginId, $pw) {
  $r = Call POST '/api/auth/login' @{ loginId = $loginId; password = $pw } $null
  if ($r.Status -ne 200) { throw "login failed for $loginId : $($r.Status) $($r.Raw)" }
  return $r.Body.data.accessToken
}

# 모든 친구가 공유하는 퀴즈 템플릿 (정답을 알고 있으므로 데모가 100% 맞힘)
$quizTemplate = @{
  questions = @(
    @{ type='MULTIPLE'; content='내가 제일 좋아하는 음식은?'; correctAnswer='치킨'; required=$true;
       options=@(@{content='치킨';correct=$true},@{content='피자';correct=$false},@{content='초밥';correct=$false},@{content='파스타';correct=$false}) },
    @{ type='OX'; content='나는 아침형 인간이다'; correctAnswer='O'; required=$false; options=@() },
    @{ type='MULTIPLE'; content='내 혈액형은?'; correctAnswer='A'; required=$false;
       options=@(@{content='A';correct=$true},@{content='B';correct=$false},@{content='O';correct=$false},@{content='AB';correct=$false}) }
  )
}

function AnswerFor($content, $type) {
  if ($content -match '음식') { return '치킨' }
  if ($content -match '혈액') { return 'A' }
  if ($type -eq 'OX' -or $content -match '아침') { return 'O' }
  return 'O'
}

$pw = 'qnect1234'

# ── 데모 계정 ──
Write-Host "`n=== DEMO ACCOUNT ===" -ForegroundColor Cyan
$demo = @{ loginId='qnectdemo'; phone='01030000000'; password=$pw; name='데모유저'; birthDate='2002-04-12' }
$r = Call POST '/api/auth/signup' $demo $null
Write-Host "signup qnectdemo -> $($r.Status)"
$demoTok = Login 'qnectdemo' $pw
$r = Call PUT '/api/profiles/me' @{ school='국민대학교'; gender='MALE'; mbti='ENFP'; drinkLevel='소주 2잔'; favoriteFood='치킨' } $demoTok
$demoId = $r.Body.data.userId
Write-Host "demo userId = $demoId, profile -> $($r.Status)"
Call PUT '/api/interests/me' @{ interestIds=@(1,2,11,21) } $demoTok | Out-Null
Call PUT '/api/profiles/me/image' @{ characterId='character05' } $demoTok | Out-Null
Call PUT '/api/quizzes/me' $quizTemplate $demoTok | Out-Null
Call POST '/api/points/attendance' $null $demoTok | Out-Null

# ── 수락될 친구 16명 (퀴즈 풀이 +10P/명으로 상점 구매 가능 잔액 확보) ──
$friends = @(
  @{ id='minseo01';  name='김민서'; g='FEMALE'; mbti='INFP'; school='연세대학교';   food='초밥';   drink='맥주 1잔';  ph='01030000001'; int=@(1,2,3) },
  @{ id='jiho02';    name='박지호'; g='MALE';   mbti='ENTP'; school='고려대학교';   food='삼겹살'; drink='소주 1병';  ph='01030000002'; int=@(4,5,6) },
  @{ id='soyeon03';  name='이소연'; g='FEMALE'; mbti='ISFJ'; school='성균관대학교'; food='파스타'; drink='안 마심';   ph='01030000003'; int=@(7,8,9) },
  @{ id='donghyun04';name='최동현'; g='MALE';   mbti='ESTJ'; school='한양대학교';   food='치킨';   drink='맥주 3잔';  ph='01030000004'; int=@(10,11,12) },
  @{ id='yujin05';   name='정유진'; g='FEMALE'; mbti='ENFJ'; school='서강대학교';   food='떡볶이'; drink='소주 2잔';  ph='01030000005'; int=@(13,14,15) },
  @{ id='taemin06';  name='강태민'; g='MALE';   mbti='INTP'; school='중앙대학교';   food='라멘';   drink='와인 1잔';  ph='01030000006'; int=@(16,17,18) },
  @{ id='hayoung07'; name='윤하영'; g='FEMALE'; mbti='ISTP'; school='경희대학교';   food='샐러드'; drink='안 마심';   ph='01030000007'; int=@(19,20,21) },
  @{ id='seojun08';  name='임서준'; g='MALE';   mbti='ESFP'; school='서울대학교';   food='피자';   drink='맥주 2잔';  ph='01030000008'; int=@(1,5,10,15) },
  @{ id='nayeon09';  name='한나연'; g='FEMALE'; mbti='INFJ'; school='이화여자대학교'; food='마라탕'; drink='소주 1잔'; ph='01030000009'; int=@(2,6,11,16) },
  @{ id='woojin10';  name='장우진'; g='MALE';   mbti='ESTP'; school='건국대학교';   food='국밥';   drink='소주 3잔';  ph='01030000010'; int=@(3,7,12,21) },
  @{ id='jihoon11';  name='서지훈'; g='MALE';   mbti='INTJ'; school='서울시립대학교'; food='규동'; drink='맥주 1잔';  ph='01030000011'; int=@(4,8,12) },
  @{ id='chaewon12'; name='문채원'; g='FEMALE'; mbti='ESFJ'; school='숙명여자대학교'; food='케이크'; drink='안 마심'; ph='01030000012'; int=@(5,9,13) },
  @{ id='minjae13';  name='오민재'; g='MALE';   mbti='ENFP'; school='아주대학교';   food='햄버거'; drink='소주 2잔';  ph='01030000013'; int=@(6,10,14) },
  @{ id='eunseo14';  name='배은서'; g='FEMALE'; mbti='ISTJ'; school='인하대학교';   food='쌀국수'; drink='맥주 2잔';  ph='01030000014'; int=@(7,11,15) },
  @{ id='hyunwoo15'; name='신현우'; g='MALE';   mbti='ESTP'; school='단국대학교';   food='곱창';   drink='소주 1병';  ph='01030000015'; int=@(8,16,20) },
  @{ id='dahye16';   name='조다혜'; g='FEMALE'; mbti='INFP'; school='세종대학교';   food='샌드위치'; drink='와인 2잔'; ph='01030000016'; int=@(9,17,21) }
)

$friendInfo = @()
Write-Host "`n=== FRIENDS (will be accepted) ===" -ForegroundColor Cyan
foreach ($f in $friends) {
  Call POST '/api/auth/signup' @{ loginId=$f.id; phone=$f.ph; password=$pw; name=$f.name; birthDate='2002-01-01' } $null | Out-Null
  $tok = Login $f.id $pw
  $pr = Call PUT '/api/profiles/me' @{ school=$f.school; gender=$f.g; mbti=$f.mbti; drinkLevel=$f.drink; favoriteFood=$f.food } $tok
  $uid = $pr.Body.data.userId
  Call PUT '/api/interests/me' @{ interestIds=$f.int } $tok | Out-Null
  Call PUT '/api/quizzes/me' $quizTemplate $tok | Out-Null
  $friendInfo += [pscustomobject]@{ loginId=$f.id; name=$f.name; userId=$uid; token=$tok }
  Write-Host ("  {0,-11} {1}  userId={2}" -f $f.id, $f.name, $uid)
}

# ── 친구 관계 수립: 데모가 요청 → 친구가 수락 ──
Write-Host "`n=== FRIENDSHIPS (demo requests, friend accepts) ===" -ForegroundColor Cyan
foreach ($fi in $friendInfo) {
  $req = Call POST '/api/friends' @{ addresseeId=$fi.userId } $demoTok
  if ($req.Status -eq 200 -or $req.Status -eq 201) {
    $fsId = $req.Body.data.friendshipId
    $acc = Call PATCH "/api/friends/$fsId/accept" $null $fi.token
    Write-Host ("  demo -> {0,-11} request#{1} accept->{2}" -f $fi.loginId, $fsId, $acc.Status)
  } else {
    Write-Host ("  demo -> {0,-11} request->{1} (이미 관계 존재, 무시)" -f $fi.loginId, $req.Status)
  }
}

# ── 데모가 각 친구의 퀴즈를 풀기 (첫 풀기 +10P/명, 케미 점수 생성) ──
Write-Host "`n=== DEMO SOLVES FRIEND QUIZZES ===" -ForegroundColor Cyan
foreach ($fi in $friendInfo) {
  $view = Call GET "/api/quizzes/owner/$($fi.userId)" $null $demoTok
  if ($view.Status -ne 200) { Write-Host ("  {0}: solvable view -> {1} (skip)" -f $fi.loginId, $view.Status); continue }
  $answers = @()
  foreach ($q in $view.Body.data.questions) {
    $answers += @{ questionId = $q.questionId; answer = (AnswerFor $q.content $q.type) }
  }
  $att = Call POST "/api/quizzes/owner/$($fi.userId)/attempts" @{ answers=$answers } $demoTok
  $score = if ($att.Body.data) { "$($att.Body.data.score)/$($att.Body.data.total) ($($att.Body.data.scorePercent)%)" } else { '-' }
  Write-Host ("  solve {0,-11} -> {1}  score={2}" -f $fi.loginId, $att.Status, $score)
}

# ── 데모: 메모 2건 ──
Call PUT '/api/friends/memos' @{ friendId=$friendInfo[0].userId; content='동아리에서 만난 친구. 기타 잘 침' } $demoTok | Out-Null
Call PUT '/api/friends/memos' @{ friendId=$friendInfo[1].userId; content='과 동기, 발표 잘함' } $demoTok | Out-Null

# ── 대기 중 친구 요청 2건 (데모가 받은 요청, 수락 안 함) ──
Write-Host "`n=== PENDING INCOMING REQUESTS (left pending) ===" -ForegroundColor Cyan
$pending = @(
  @{ id='hyewon20'; name='한혜원'; g='FEMALE'; mbti='ENTJ'; school='홍익대학교'; ph='01030000020' },
  @{ id='junseo21'; name='오준서'; g='MALE';   mbti='ISFP'; school='동국대학교'; ph='01030000021' }
)
foreach ($p in $pending) {
  Call POST '/api/auth/signup' @{ loginId=$p.id; phone=$p.ph; password=$pw; name=$p.name; birthDate='2003-03-03' } $null | Out-Null
  $tok = Login $p.id $pw
  $pr = Call PUT '/api/profiles/me' @{ school=$p.school; gender=$p.g; mbti=$p.mbti; drinkLevel='맥주 1잔'; favoriteFood='치킨' } $tok
  $req = Call POST '/api/friends' @{ addresseeId=$demoId } $tok
  Write-Host ("  {0,-10} -> demo  request->{1}" -f $p.id, $req.Status)
}

# ── 데일리 답변 ──
Write-Host "`n=== DAILY ===" -ForegroundColor Cyan
$d = Call POST '/api/daily/today/answer' @{ selected='A' } $demoTok
Write-Host "  daily answer A -> $($d.Status)"

# ── 상점: 잔액 확인 후 살 수 있는 가장 싼 아이템 구매 + 장착 ──
Write-Host "`n=== SHOP ===" -ForegroundColor Cyan
$bal = (Call GET '/api/points/balance' $null $demoTok).Body.data.balance
Write-Host "  demo balance = $bal P"
$items = (Call GET '/api/shop/items' $null $demoTok).Body.data
$affordable = $items | Where-Object { $_.price -le $bal } | Sort-Object price | Select-Object -First 1
if ($affordable) {
  $buy = Call POST "/api/shop/items/$($affordable.itemId)/purchase" $null $demoTok
  Write-Host ("  purchase '{0}' ({1}P) -> {2}" -f $affordable.name, $affordable.price, $buy.Status)
  $uiId = $buy.Body.data.userItemId
  if ($uiId) {
    $eq = Call PATCH "/api/shop/my-items/$uiId/equip" $null $demoTok
    Write-Host "  equip userItemId=$uiId -> $($eq.Status)"
  }
} else {
  Write-Host "  (살 수 있는 아이템 없음 - 잔액 부족)"
}

# ── 그룹 4개 (멤버 포함) ──
Write-Host "`n=== GROUPS ===" -ForegroundColor Cyan
$u = @{}
foreach ($fi in $friendInfo) { $u[$fi.loginId] = $fi.userId }
$groups = @(
  @{ name='대학동기';     tags=@('동기','CC');     members=@($u['minseo01'],$u['jiho02'],$u['soyeon03'],$u['jihoon11']) },
  @{ name='밴드동아리';   tags=@('밴드','공연');   members=@($u['jiho02'],$u['donghyun04'],$u['taemin06'],$u['minjae13']) },
  @{ name='토익스터디';   tags=@('토익','자격증'); members=@($u['soyeon03'],$u['yujin05'],$u['eunseo14']) },
  @{ name='MT크루';       tags=@('여행','술');     members=@($u['minseo01'],$u['donghyun04'],$u['yujin05'],$u['taemin06'],$u['woojin10'],$u['hyunwoo15']) },
  @{ name='맛집탐방';     tags=@('맛집','먹스타'); members=@($u['nayeon09'],$u['seojun08'],$u['chaewon12'],$u['dahye16']) }
)
foreach ($g in $groups) {
  $gr = Call POST '/api/groups/with-members' @{ name=$g.name; hashtags=$g.tags; friendIds=$g.members } $demoTok
  Write-Host ("  group '{0}' ({1}명) -> {2}" -f $g.name, $g.members.Count, $gr.Status)
}

# ── 최종 요약 ──
Write-Host "`n================= SUMMARY =================" -ForegroundColor Yellow
$fl = (Call GET '/api/friends' $null $demoTok).Body.data
$gl = (Call GET '/api/groups' $null $demoTok).Body.data
$recv = (Call GET '/api/friends/requests/received' $null $demoTok).Body.data
$bal2 = (Call GET '/api/points/balance' $null $demoTok).Body.data.balance
$noti = (Call GET '/api/notifications' $null $demoTok).Body.data
Write-Host ("로그인 아이디 : qnectdemo")
Write-Host ("비밀번호      : $pw")
Write-Host ("userId        : $demoId")
Write-Host ("친구 수       : $($fl.Count)")
Write-Host ("그룹 수       : $($gl.Count)")
Write-Host ("받은(대기) 요청: $($recv.Count)")
Write-Host ("포인트 잔액   : $bal2 P")
Write-Host ("알림 수       : $($noti.Count)")
