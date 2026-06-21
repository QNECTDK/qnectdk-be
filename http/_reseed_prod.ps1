# prod demouser의 기존 시드친구(pseedf1~6) 친구관계를 정리하고, 현실적인 이름의 친구로 재구성.
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

Write-Host "=== 기존 시드친구(pseedf1~6) 친구관계 정리 ===" -ForegroundColor Cyan
foreach($id in @('pseedf1','pseedf2','pseedf3','pseedf4','pseedf5','pseedf6')){
  $tok=Login $id
  if(-not $tok){ Write-Host "  $id 없음/스킵"; continue }
  $uid=(Call GET '/api/profiles/me' $null $tok).Body.data.userId
  $r=Call DELETE "/api/friends/$uid" $null $demo
  Write-Host ("  demouser -x {0}(uid={1}) -> {2}" -f $id,$uid,$r.Status)
}

# 현실적인 이름의 새 친구 (문항수/응시모드 다양화)
$defs = @(
  @{id='kimseoyeon'; name='김서연'; ph='01058880011';g='FEMALE';mbti='INFP';school='연세대학교';   food='초밥';   n=3; mode='full'},
  @{id='leejunho';   name='이준호'; ph='01058880012';g='MALE';  mbti='ENTP';school='고려대학교';   food='삼겹살'; n=4; mode='partial'},
  @{id='parkjimin';  name='박지민'; ph='01058880013';g='FEMALE';mbti='ISFJ';school='성균관대학교'; food='파스타'; n=5; mode='none'},
  @{id='choiyuna';   name='최유나'; ph='01058880014';g='FEMALE';mbti='ENFJ';school='서강대학교';   food='떡볶이'; n=3; mode='partial'},
  @{id='jeongdohyun';name='정도현'; ph='01058880015';g='MALE';  mbti='ESTJ';school='한양대학교';   food='치킨';   n=4; mode='full'},
  @{id='hansohee';   name='한소희'; ph='01058880016';g='FEMALE';mbti='INTP';school='중앙대학교';   food='라멘';   n=5; mode='partial'}
)

$info=@(); $di=0
Write-Host "`n=== 새 친구 생성 + 프로필/퀴즈 ===" -ForegroundColor Cyan
foreach($f in $defs){
  Call POST '/api/auth/signup' @{loginId=$f.id;phone=$f.ph;password=$pw;name=$f.name;birthDate='2002-01-01'} $null | Out-Null
  $tok=Login $f.id
  if(-not $tok){ Write-Host "  $($f.id) 로그인 실패 → 스킵" -ForegroundColor Yellow; continue }
  $uid=(Call PUT '/api/profiles/me' @{school=$f.school;gender=$f.g;mbti=$f.mbti;drinkLevel='맥주 1잔';favoriteFood=$f.food} $tok).Body.data.userId
  Call PUT '/api/quizzes/me' (QuizBody $f.n) $tok | Out-Null
  $info += [pscustomobject]@{ name=$f.name; uid=$uid; tok=$tok; n=$f.n; mode=$f.mode }
  Write-Host ("  {0} userId={1} {2}문항" -f $f.name,$uid,$f.n)
}

Write-Host "`n=== demouser 친구 추가 ===" -ForegroundColor Cyan
foreach($f in $info){ $r=Call POST '/api/friends' @{friendId=$f.uid} $demo; Write-Host ("  + {0} -> {1}" -f $f.name,$r.Status) }

Write-Host "`n=== demouser 응시(완료/부분/미응시) ===" -ForegroundColor Cyan
foreach($f in $info){
  if($f.mode -eq 'none'){ Write-Host ("  {0} {1}문항 -> 미응시" -f $f.name,$f.n); continue }
  $qs=(Call GET "/api/quizzes/owner/$($f.uid)" $null $demo).Body.data.questions
  $answers=@(); for($j=0;$j -lt $qs.Count;$j++){ $ans=if($f.mode -eq 'full' -or $j -lt [math]::Ceiling($qs.Count/2)){CorrectFor $qs[$j].content}else{'오답'}; $answers+=@{questionId=$qs[$j].questionId;answer=$ans} }
  $att=Call POST "/api/quizzes/owner/$($f.uid)/attempts" @{answers=$answers} $demo
  Write-Host ("  {0} {1}문항 {2} -> {3}/{4}" -f $f.name,$f.n,$f.mode,$att.Body.data.score,$att.Body.data.total)
}

Write-Host "`n=== 친구들 데일리 답변 + 메모 ===" -ForegroundColor Cyan
foreach($f in $info){ $sel=if($di%2 -eq 0){'A'}else{'B'};$di++; Call POST '/api/daily/today/answer' @{selected=$sel} $f.tok | Out-Null }
if($info.Count -ge 1){ Call PUT '/api/friends/memos' @{friendId=$info[0].uid;content='새터에서 만난 친구, 노래 잘함'} $demo | Out-Null }
if($info.Count -ge 2){ Call PUT '/api/friends/memos' @{friendId=$info[1].uid;content='과 동기, 발표 잘함'} $demo | Out-Null }

$fr=(Call GET '/api/friends' $null $demo).Body.data
$fq=(Call GET '/api/quizzes/friends' $null $demo).Body.data
$cnts=($fq|Group-Object totalQuestions|%{"$($_.Name)Q×$($_.Count)"}) -join ','
Write-Host ("`n[결과] demouser 친구={0} / 문항분포={1} / 완료={2}" -f $fr.Count,$cnts,($fq|?{$_.attempted}).Count) -ForegroundColor Yellow
