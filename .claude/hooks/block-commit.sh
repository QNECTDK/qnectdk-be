#!/usr/bin/env bash
# PreToolUse 훅 — 프로젝트 규칙: 임의 커밋 금지.
# git commit / --amend / force-push / reset --hard 를 기본 차단한다.
# 단, 같은 .claude 폴더에 .commit-allowed 마커가 있으면 허용(세션 단위 opt-in).
# 어떤 오류가 나도 fail-open(exit 0)으로 워크플로를 막지 않는다.
#
# WSL/Linux(bash) 기준. 이전 PowerShell 훅을 대체한다.

set +e

# 이 스크립트: <root>/.claude/hooks/block-commit.sh  →  마커: <root>/.claude/.commit-allowed
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)" || exit 0
marker="${script_dir}/../.commit-allowed"

payload="$(cat)"
[ -z "$payload" ] && exit 0

# tool_input.command 추출 (python3 우선, 실패하면 원문 전체를 대상으로 매칭)
command_text="$(printf '%s' "$payload" | python3 -c '
import sys, json
try:
    data = json.load(sys.stdin)
    print(data.get("tool_input", {}).get("command", ""), end="")
except Exception:
    pass
' 2>/dev/null)"
[ -z "$command_text" ] && command_text="$payload"

is_blocked=0
printf '%s' "$command_text" | grep -Eq 'git[[:space:]]+commit' && is_blocked=1
printf '%s' "$command_text" | grep -Eq 'git[[:space:]]+commit[^|;&]*--amend' && is_blocked=1
printf '%s' "$command_text" | grep -Eq 'git[[:space:]]+push[^|;&]*(--force|--force-with-lease|[[:space:]]-f([[:space:]]|$))' && is_blocked=1
printf '%s' "$command_text" | grep -Eq 'git[[:space:]]+reset[^|;&]*--hard' && is_blocked=1

[ "$is_blocked" -eq 0 ] && exit 0
[ -f "$marker" ] && exit 0

# 차단: PreToolUse deny 결정을 JSON으로 출력 (python3로 안전하게 escape)
python3 -c '
import json
reason = ("프로젝트 규칙: 임의 커밋 금지. "
          "사용자가 명시적으로 허용한 세션에서만 커밋하세요 "
          "(.claude/.commit-allowed 마커가 있어야 합니다).")
print(json.dumps({
    "hookSpecificOutput": {
        "hookEventName": "PreToolUse",
        "permissionDecision": "deny",
        "permissionDecisionReason": reason,
    }
}))
' 2>/dev/null
exit 0
