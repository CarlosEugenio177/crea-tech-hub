import type { ReactNode } from 'react'
import styles from './ListPage.module.css'
import { PageHeader } from './PageHeader.tsx'

type ListPageProps = {
  eyebrow: string
  title: string
  description: string
  actions?: ReactNode
  children: ReactNode
}

export function ListPage({
  eyebrow,
  title,
  description,
  actions,
  children,
}: ListPageProps) {
  return (
    <div className={styles.page}>
      <div className={styles.heading}>
        <PageHeader eyebrow={eyebrow} title={title} description={description} />
        {actions && <div className={styles.actions}>{actions}</div>}
      </div>
      {children}
    </div>
  )
}
