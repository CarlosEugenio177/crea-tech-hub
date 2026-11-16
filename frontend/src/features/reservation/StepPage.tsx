import type { ReactNode } from 'react'
import { PageHeader } from '../../shared/ui/PageHeader.tsx'
import styles from './StepPage.module.css'
import { StepProgress } from './StepProgress.tsx'
import { STEPS } from './steps.ts'

type StepPageProps = {
  step: number
  title: string
  description: string
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
      <PageHeader
        eyebrow={`Nova solicitação · Etapa ${step} de ${STEPS.length}`}
        title={title}
        description={description}
      />
      {children}
    </div>
  )
}
