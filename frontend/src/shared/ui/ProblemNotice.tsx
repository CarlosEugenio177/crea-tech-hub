import { problemMessage } from '../api/problem.ts'
import { Notice } from './Notice.tsx'

type ProblemNoticeProps = { error: unknown }

export function ProblemNotice({ error }: ProblemNoticeProps) {
  if (!error) return null
  return <Notice tone="error">{problemMessage(error)}</Notice>
}
