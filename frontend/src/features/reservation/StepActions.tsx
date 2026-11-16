import type { ReactNode } from 'react'
import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import styles from './StepActions.module.css'

type StepActionsProps = {
  backTo: string
  backLabel: string
  children: ReactNode
}

export function StepActions({ backTo, backLabel, children }: StepActionsProps) {
  return (
    <div className={styles.actions}>
      <ButtonLink variant="secondary" to={backTo}>
        {backLabel}
      </ButtonLink>
      {children}
    </div>
  )
}
