# 배포 서버(prod)의 demouser 계정에 시연 시나리오를 채운다(비파괴적).
# 새 데모 친구 6명을 만들어 demouser와 친구 맺고: 퀴즈 문항수 3/4/5 + 완료/부분/미응시 + 데일리 답변 + 메모.
# (홈 리마인드 카드 / QUIZ_REMIND 알림은 서버 DB 직접 수정이 필요해 이 방식으론 제외)
$ErrorActionPreference = 'Stop'
$base = 'https://qnectdk.duckdns.org'; $pw = 'qnect1234'
function Call($m,$p,$b,$t){$h=@{};if($t){$h['Authorization']="Bearer $t"};$pa=@{Method=$m;Uri="$base$p";Headers=$h;SkipHttpErrorCheck=$true;TimeoutSec=30};if($null -ne $b){$pa['ContentType']='application/json';$pa['Body']=($b|ConvertTo-Json -Depth 12)};$r=Invoke-WebRequest @pa;$o=$null;try{$o=$r.Content|ConvertFrom-Json}catch{};return [pscustomobject]@{Status=[int]$r.StatusCode;Body=$o}}
function Login($id){(Call POST '/api/auth/login' @{loginId=$id;password=$pw} $null).Body.data.accessToken}
function QuizBody($n){
  $q = @(
    @{type='MULTIPLE';content='가장 좋아하는 음식은?';correctAnswer='치킨';required=$true;options=@(@{content='치킨';correct=$true},@{content='피자';correct=$false},@{content='초밥';correct=$false},@{content='파스타';correct=$false})},
    @{type='OX';content='나는 아침형 인간이다';correctAnswer='O';required=$false;options=@()},
    @{type='MULTIPLE';content='내 혈액형은?';correctAnswer='A';required=$false;options=@(@{content='A';correct=$true},@{content='B';correct=$false},@{content='O';correct=$false},@{content='AB';correct=$false})},
    @{type='OX';content='나는 운동을 좋아한다';correctAnswer='O';required=$false;options=@()},
    @{type='MULTIPLE';content='좋아하는 계절은?';correctAnswer='여름';required=$false;options=@(@{content='봄';correct=$false},@{content='여름';correct=$true},@{content='가을';correct=$false},@{content='겨울';correct=$false})}
  )
  return @{ questions = $q[0..($n-1)] }
}
function CorrectFor($c){ if($c -match '음식'){'치킨'}elseif($c -match '아침'){'O'}elseif($c -match '혈액'){'A'}elseif($c -match '운동'){'O'}elseif($c -match '계절'){'여름'}else{'O'} }

$demo = Login 'demouser'
if(-not $demo){ Write-Host 'demouser 로그인 실패' -ForegroundColor Red; exit 1 }

# 새 데모 친구 6명 (문항수/응시모드 다양화)
$defs = @(
  @{id='pseedf1';name='시드친구1';ph='01058880001';g='FEMALE';mbti='INFP';school='연세대학교';food='초밥'; n=3; mode='full'},
  @{id='pseedf2';name='시드친구2';ph='01058880002';g='MALE';  mbti='ENTP';school='고려대학교';food='삼겹살';n=4; mode='partial'},
  @{id='pseedf3';name='시드친구3';ph='01058880003';g='FEMALE';mbti='ISFJ';school='성균관대학교';food='파스타';n=5; mode='none'},
  @{id='pseedf4';name='시드친구4';ph='01058880004';g='MALE';  mbti='ESTJ';school='한양대학교';food='치킨'; n=3; mode='partial'},
  @{id='pseedf5';name='시드친구5';ph='01058880005';g='FEMALE';mbti='ENFJ';school='서강대학교';food='떡볶이';n=4; mode='full'},
  @{id='pseedf6';name='시드친구6';ph='01058880006';g='MALE';  mbti='INTP';school='중앙대학교';food='라멘'; n=5; mode='partial'}
)

$info=@(); $di=0
Write-Host "`n=== 데모 친구 생성 + 프로필/퀴즈 ===" -ForegroundColor Cyan
foreach($f in $defs){
  Call POST '/api/auth/signup' @{loginId=$f.id;phone=$f.ph;password=$pw;name=$f.name;birthDate='2002-01-01'} $null | Out-Null
  $tok = Login $f.id
  if(-not $tok){ Write-Host "  $($f.id) 로그인 실패(중복 전화번호 등) → 건너뜀" -ForegroundColor Yellow; continue }
  $uid = (Call PUT '/api/profiles/me' @{school=$f.school;gender=$f.g;mbti=$f.mbti;drinkLevel='맥주 1잔';favoriteFood=$f.food} $tok).Body.data.userId
  Call PUT '/api/quizzes/me' (QuizBody $f.n) $tok | Out-Null
  $info += [pscustomobject]@{ id=$f.id; uid=$uid; tok=$tok; n=$f.n; mode=$f.mode }
  Write-Host ("  {0,-8} userId={1} {2}문항" -f $f.id,$uid,$f.n)
}

Write-Host "`n=== demouser가 친구 추가(수락=상호등록) ===" -ForegroundColor Cyan
foreach($f in $info){
  $r = Call POST '/api/friends' @{friendId=$f.uid} $demo
  Write-Host ("  + {0} -> {1}" -f $f.id,$r.Status)
}

Write-Host "`n=== demouser 응시: 완료/부분/미응시 ===" -ForegroundColor Cyan
foreach($f in $info){
  if($f.mode -eq 'none'){ Write-Host ("  {0,-8} {1}문항 -> 미응시" -f $f.id,$f.n); continue }
  $qs = (Call GET "/api/quizzes/owner/$($f.uid)" $null $demo).Body.data.questions
  $answers=@()
  for($j=0;$j -lt $qs.Count;$j++){
    $ans = if($f.mode -eq 'full' -or $j -lt [math]::Ceiling($qs.Count/2)){ CorrectFor $qs[$j].content } else { '오답' }
    $answers += @{ questionId=$qs[$j].questionId; answer=$ans }
  }
  $att = Call POST "/api/quizzes/owner/$($f.uid)/attempts" @{answers=$answers} $demo
  Write-Host ("  {0,-8} {1}문항 {2} -> {3}/{4}" -f $f.id,$f.n,$f.mode,$att.Body.data.score,$att.Body.data.total)
}

Write-Host "`n=== 친구들 오늘 데일리 답변(친구 투표결과 보강) ===" -ForegroundColor Cyan
foreach($f in $info){
  $sel = if($di % 2 -eq 0){'A'}else{'B'}; $di++
  Call POST '/api/daily/today/answer' @{selected=$sel} $f.tok | Out-Null
}
Write-Host "  완료"

Write-Host "`n=== 메모 ===" -ForegroundColor Cyan
if($info.Count -ge 1){ Call PUT '/api/friends/memos' @{friendId=$info[0].uid;content='새로 알게 된 친구, 기타 잘 침'} $demo | Out-Null }
if($info.Count -ge 2){ Call PUT '/api/friends/memos' @{friendId=$info[1].uid;content='과 동기, 발표 잘함'} $demo | Out-Null }
Write-Host "  메모 작성"

# 최종 확인
$fr=(Call GET '/api/friends' $null $demo).Body.data
$fq=(Call GET '/api/quizzes/friends' $null $demo).Body.data
$cnts=($fq|Group-Object totalQuestions|%{"$($_.Name)Q×$($_.Count)"}) -join ','
Write-Host ("`n[결과] demouser 친구={0} / 문항분포={1} / 완료={2}" -f $fr.Count,$cnts,($fq|?{$_.attempted}).Count) -ForegroundColor Yellow
