import type { ReactNode, SubmitEventHandler } from 'react'
import styles from './FormCard.module.css'

type FormCardProps = {
  onSubmit: SubmitEventHandler<HTMLFormElement>
  onChange?: () => void
  children: ReactNode
}

export function FormCard({ onSubmit, onChange, children }: FormCardProps) {
  return (
    <form
      className={styles.form}
      onSubmit={onSubmit}
      onChange={onChange}
      noValidate
    >
      {children}
    </form>
  )
}
