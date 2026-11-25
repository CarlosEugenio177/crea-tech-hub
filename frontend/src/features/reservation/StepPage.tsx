import type { ReactNode } from 'react'
import styles from './StepPage.module.css'
import { StepProgress } from './StepProgress.tsx'

type StepPageProps = {
  step: number
  title: string
  description?: string
  children: ReactNode
}

export function StepPage({
  step,
  title,
  description,
  children,
}: StepPageProps) {
  return (
    <div className={styles.page}>
      <StepProgress current={step} />
      <header className={styles.heading}>
        <h1>{title}</h1>
        {description && <p>{description}</p>}
      </header>
      {children}
    </div>
  )
}
