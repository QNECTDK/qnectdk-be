#!/usr/bin/env bash
# PostToolUse(Write|Edit) 훅 — A 컨벤션 가벼운 정적 점검(비차단, 경고만).
# 항상 exit 0. 위반 의심 시 stdout에 경고만 출력해 주의를 환기한다(차단하지 않음).
# 무거운 검증은 compile-check.sh / convention-reviewer 에이전트가 담당.
# WSL/Linux(bash) 기준.

set +e

payload="$(cat)"
[ -z "$payload" ] && exit 0

file_path="$(printf '%s' "$payload" | python3 -c '
import sys, json
try:
    print(json.load(sys.stdin).get("tool_input", {}).get("file_path", ""), end="")
except Exception:
    pass
' 2>/dev/null)"

case "$file_path" in
  *.java) ;;
  *) exit 0 ;;
esac
[ -f "$file_path" ] || exit 0

base="$(basename "$file_path")"
warn() { echo "⚠ [convention] $1"; }

# 1) @RestControllerAdvice 는 GlobalExceptionHandler 하나만
if grep -q '@RestControllerAdvice' "$file_path" && [ "$base" != "GlobalExceptionHandler.java" ]; then
  warn "새 @RestControllerAdvice 감지($base). 핸들러는 GlobalExceptionHandler 하나만 — 거기에 메서드를 추가하세요."
fi

# 2) 엔티티에 @Setter 금지
if grep -q '@Entity' "$file_path" && grep -q '@Setter' "$file_path"; then
  warn "엔티티에 @Setter 감지($base). setter 금지 — 상태 변경은 도메인 메서드로."
fi

# 3) 컨트롤러는 모든 응답을 ApiResponse 로 감싼다(휴리스틱)
case "$base" in
  *Controller.java)
    if ! grep -q 'global\.response\.ApiResponse' "$file_path"; then
      warn "$base 가 ApiResponse 를 import하지 않음. 모든 응답을 ApiResponse<T>로 감쌌는지 확인하세요."
    fi
    ;;
esac

# 4) 도메인 경계: 타 도메인 엔티티 import 금지 (Long ID / 서비스 호출로)
mydomain="$(printf '%s' "$file_path" | sed -n 's#.*/domain/\([^/]*\)/.*#\1#p')"
if [ -n "$mydomain" ]; then
  bad="$(grep -oE 'import com\.qnectdk\.domain\.[a-zA-Z0-9_]+\.entity\.[A-Za-z0-9_]+' "$file_path" \
        | grep -v "domain\.${mydomain}\.entity\.")"
  if [ -n "$bad" ]; then
    warn "타 도메인 엔티티 import 감지 — 경계는 Long ID/서비스 호출로 바꾸세요:"
    printf '%s\n' "$bad" | sed 's/^/    /'
  fi
fi

exit 0
