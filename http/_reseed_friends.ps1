# 방향성 친구모델 전환 후, 데모 계정(qnectdemo)의 친구 16명을 다시 등록한다.
# addFriend(demo→friend)가 (demo↔friend) 두 방향을 함께 만들어 상호 친구가 된다.
# 반드시 "새 코드로 BE 재기동 + friendships 테이블 재생성" 이후에 실행할 것.
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$pw = 'qnect1234'

function Login($id) {
  (Invoke-RestMethod -Method POST -Uri "$base/api/auth/login" -ContentType 'application/json' `
    -Body (@{ loginId = $id; password = $pw } | ConvertTo-Json)).data.accessToken
}

$demo = Login 'qnectdemo'
$friends = @(
  'minseo01','jiho02','soyeon03','donghyun04','yujin05','taemin06','hayoung07','seojun08',
  'nayeon09','woojin10','jihoon11','chaewon12','minjae13','eunseo14','hyunwoo15','dahye16'
)

foreach ($f in $friends) {
  $tok = Login $f
  $uid = (Invoke-RestMethod -Uri "$base/api/profiles/me" -Headers @{ Authorization = "Bearer $tok" }).data.userId
  $r = Invoke-WebRequest -Method POST -Uri "$base/api/friends" -Headers @{ Authorization = "Bearer $demo" } `
    -ContentType 'application/json' -Body (@{ friendId = $uid } | ConvertTo-Json) -SkipHttpErrorCheck
  Write-Host ("  {0,-11} userId={1} -> {2}" -f $f, $uid, [int]$r.StatusCode)
}

$cnt = (Invoke-RestMethod -Uri "$base/api/friends" -Headers @{ Authorization = "Bearer $demo" }).data.Count
Write-Host "demo 친구 수 = $cnt" -ForegroundColor Yellow
