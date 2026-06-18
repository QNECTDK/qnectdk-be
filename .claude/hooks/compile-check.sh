#!/usr/bin/env bash
# PostToolUse(Write|Edit) 훅 — .java 편집 후 컴파일 점검.
# 통과 시 무출력(exit 0). 실패 시 컴파일 에러를 노출(exit 2)해 다음 진행 전 수정 유도.
# 인프라 오류(gradlew 없음/실행 불가 등)는 fail-open(exit 0)으로 워크플로를 막지 않는다.
# WSL/Linux(bash) 기준. settings.json 변경은 새 세션부터 활성화.

set +e

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)" || exit 0
root="${CLAUDE_PROJECT_DIR:-$(cd "${script_dir}/../.." >/dev/null 2>&1 && pwd)}"
[ -z "$root" ] && exit 0

payload="$(cat)"
[ -z "$payload" ] && exit 0

# tool_input.file_path 추출 (python3 우선)
file_path="$(printf '%s' "$payload" | python3 -c '
import sys, json
try:
    print(json.load(sys.stdin).get("tool_input", {}).get("file_path", ""), end="")
except Exception:
    pass
' 2>/dev/null)"

# .java가 아니면 점검 생략
case "$file_path" in
  *.java) ;;
  *) exit 0 ;;
esac

[ -x "${root}/gradlew" ] || exit 0

out="$(cd "$root" && ./gradlew compileJava -q --console=plain 2>&1)"
status=$?
[ "$status" -eq 0 ] && exit 0

# 실패: 에러를 stderr로 노출하고 exit 2 (PostToolUse는 stderr를 모델에 전달)
{
  echo "[compile-check] ./gradlew compileJava 실패 — 진행 전 수정이 필요합니다:"
  printf '%s\n' "$out" | tail -n 40
} >&2
exit 2
