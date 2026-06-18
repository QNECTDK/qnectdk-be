#!/usr/bin/env bash
# Stop 훅 — 세션 종료 전 전체 컴파일 정합성 점검(main + test 소스).
# build(테스트 실행)는 MySQL이 필요하므로, DB가 불필요한 compileJava + compileTestJava만 수행한다.
# 통과 시 무출력(exit 0). 실패 시 에러 노출 + exit 2(종료 보류)로 깨진 채 끝나는 것을 막는다.
# 인프라 오류(gradlew 없음 등)는 fail-open(exit 0).
# WSL/Linux(bash) 기준.

set +e

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)" || exit 0
root="${CLAUDE_PROJECT_DIR:-$(cd "${script_dir}/../.." >/dev/null 2>&1 && pwd)}"
[ -z "$root" ] && exit 0
[ -x "${root}/gradlew" ] || exit 0

out="$(cd "$root" && ./gradlew compileJava compileTestJava -q --console=plain 2>&1)"
status=$?
[ "$status" -eq 0 ] && exit 0

{
  echo "[integrity-check] 컴파일 실패 — 세션 종료 전 수정을 권장합니다:"
  printf '%s\n' "$out" | tail -n 40
} >&2
exit 2
