import type { ReactNode, SubmitEventHandler } from 'react'
import styles from './FormCard.module.css'

type FormCardProps = {
  onSubmit: SubmitEventHandler<HTMLFormElement>
  children: ReactNode
}

export function FormCard({ onSubmit, children }: FormCardProps) {
  return (
    <form className={styles.form} onSubmit={onSubmit} noValidate>
      {children}
    </form>
  )
}
