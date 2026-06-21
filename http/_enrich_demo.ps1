# 데모(qnectdemo) 풍부화: 친구 퀴즈 문항수 3/4/5 다양화 + 데모가 완료/부분정답/미응시 섞기
#  + 데모·친구들 오늘 데일리 답변(친구 투표결과용) + 메모.
# (홈 리마인드 due / QUIZ_REMIND·DAILY_QUIZ 알림은 시간기반/DB라 별도로 처리 — 아래 _enrich_demo_db 참고)
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'; $pw = 'qnect1234'
function Call($m,$p,$b,$t){$h=@{};if($t){$h['Authorization']="Bearer $t"};$pa=@{Method=$m;Uri="$base$p";Headers=$h;SkipHttpErrorCheck=$true};if($null -ne $b){$pa['ContentType']='application/json';$pa['Body']=($b|ConvertTo-Json -Depth 12)};$r=Invoke-WebRequest @pa;$o=$null;try{$o=$r.Content|ConvertFrom-Json}catch{};return [pscustomobject]@{Status=[int]$r.StatusCode;Body=$o}}
function Login($id){(Call POST '/api/auth/login' @{loginId=$id;password=$pw} $null).Body.data.accessToken}

# n개(3~5) 문항 퀴즈 본문 (정답 규칙: 음식=치킨, 아침=O, 혈액=A, 운동=O, 계절=여름)
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

$demo = Login 'qnectdemo'
$demoId = (Call GET '/api/profiles/me' $null $demo).Body.data.userId

$friends = @('minseo01','jiho02','soyeon03','donghyun04','yujin05','taemin06','hayoung07','seojun08','nayeon09','woojin10','jihoon11','chaewon12','minjae13','eunseo14','hyunwoo15','dahye16')
# idx별 문항수: 0~4=3문항, 5~10=4문항, 11~15=5문항
$counts = @(3,3,3,3,3, 4,4,4,4,4,4, 5,5,5,5,5)
# idx별 데모 응시 모드: full / partial / none
$modes  = @('full','full','full','partial','partial', 'full','full','partial','none','none','none', 'full','full','partial','none','none')

$info = @()
Write-Host "`n=== 친구 퀴즈 문항수 다양화(3/4/5) ===" -ForegroundColor Cyan
for($i=0;$i -lt $friends.Count;$i++){
  $tok = Login $friends[$i]
  $uid = (Call GET '/api/profiles/me' $null $tok).Body.data.userId
  Call PUT '/api/quizzes/me' (QuizBody $counts[$i]) $tok | Out-Null
  $info += [pscustomobject]@{ login=$friends[$i]; uid=$uid; count=$counts[$i]; mode=$modes[$i] }
}
Write-Host "  완료 (3문항x5, 4문항x6, 5문항x5)"

# 데모의 기존 응시기록 제거(전부 100% 완료 상태였음) — 로컬 전용
Write-Host "`n=== 데모 기존 응시기록 초기화 (로컬 DB) ===" -ForegroundColor Cyan
docker exec qnectdb-mysql mysql -uroot -pqnectpass qnectdb -e "DELETE FROM quiz_answers WHERE attempt_id IN (SELECT id FROM quiz_attempts WHERE solver_id=$demoId); DELETE FROM quiz_attempts WHERE solver_id=$demoId;" 2>$null
Write-Host "  초기화 완료"

Write-Host "`n=== 데모가 완료/부분정답/미응시 섞어서 응시 ===" -ForegroundColor Cyan
foreach($f in $info){
  if($f.mode -eq 'none'){ Write-Host ("  {0,-11} {1}문항 -> 미응시" -f $f.login,$f.count); continue }
  $view = Call GET "/api/quizzes/owner/$($f.uid)" $null $demo
  $qs = $view.Body.data.questions
  $half = [math]::Floor($qs.Count/2)
  $answers=@()
  for($j=0;$j -lt $qs.Count;$j++){
    $correct = CorrectFor $qs[$j].content
    $ans = if($f.mode -eq 'full' -or $j -lt [math]::Ceiling($qs.Count/2)){ $correct } else { '오답' }
    $answers += @{ questionId=$qs[$j].questionId; answer=$ans }
  }
  $att = Call POST "/api/quizzes/owner/$($f.uid)/attempts" @{answers=$answers} $demo
  Write-Host ("  {0,-11} {1}문항 {2} -> {3}/{4}" -f $f.login,$f.count,$f.mode,$att.Body.data.score,$att.Body.data.total)
}

Write-Host "`n=== 오늘 데일리: 데모 + 친구들 답변(친구 투표결과용) ===" -ForegroundColor Cyan
Call POST '/api/daily/today/answer' @{selected='A'} $demo | Out-Null
$di=0
foreach($f in $info){
  if($di -ge 10){break}
  $tok = Login $f.login
  $sel = if($di % 2 -eq 0){'A'}else{'B'}
  Call POST '/api/daily/today/answer' @{selected=$sel} $tok | Out-Null
  $di++
}
$stats = Call GET '/api/daily/today/stats' $null $demo
Write-Host ("  전체 A {0}% / B {1}% · 친구 선택 {2}명" -f $stats.Body.data.overall.percentA, $stats.Body.data.overall.percentB, $stats.Body.data.friends.selections.Count)

Write-Host "`n=== 친구 메모 ===" -ForegroundColor Cyan
Call PUT '/api/friends/memos' @{friendId=$info[0].uid; content='동아리에서 만난 친구. 기타 잘 침'} $demo | Out-Null
Call PUT '/api/friends/memos' @{friendId=$info[1].uid; content='과 동기, 발표 잘함'} $demo | Out-Null
Call PUT '/api/friends/memos' @{friendId=$info[2].uid; content='스터디 같이 함'} $demo | Out-Null
Write-Host "  메모 3건 작성"

Write-Host "`nDONE. demoId=$demoId" -ForegroundColor Yellow
