import type { ReactNode } from 'react'
import styles from './Notice.module.css'

type NoticeProps = {
  tone: 'error' | 'success' | 'info'
  children: ReactNode
}

export function Notice({ tone, children }: NoticeProps) {
  return (
    <p
      className={`${styles.notice} ${styles[tone]}`}
      role={tone === 'error' ? 'alert' : 'status'}
    >
      {children}
    </p>
  )
}
