import type { ReactNode } from 'react'
import { ProblemNotice } from './ProblemNotice.tsx'
import styles from './RecordCard.module.css'
import { StatusBadge } from './StatusBadge.tsx'

type RecordCardProps = {
  title: string
  active?: boolean
  actions?: ReactNode
  error?: unknown
  children: ReactNode
}

export function RecordCard({
  title,
  active,
  actions,
  error,
  children,
}: RecordCardProps) {
  return (
    <li className={styles.card}>
      <div className={styles.summary}>
        <div className={styles.title}>
          <h2 className={styles.name}>{title}</h2>
          {active !== undefined && <StatusBadge active={active} />}
        </div>
        {children}
      </div>
      {actions && <div className={styles.actions}>{actions}</div>}
      <ProblemNotice error={error} />
    </li>
  )
}
