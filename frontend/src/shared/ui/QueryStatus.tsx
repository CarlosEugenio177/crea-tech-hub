import { Notice } from './Notice.tsx'
import { ProblemNotice } from './ProblemNotice.tsx'

type QueryStatusProps = { error: unknown }

export function QueryStatus({ error }: QueryStatusProps) {
  if (error) return <ProblemNotice error={error} />
  return <Notice tone="info">Carregando…</Notice>
}
