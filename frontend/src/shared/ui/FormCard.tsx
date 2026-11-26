import type { ReactNode, SubmitEventHandler } from 'react'
import styles from './FormCard.module.css'

type FormCardProps = {
  onSubmit: SubmitEventHandler<HTMLFormElement>
  onChange?: () => void
  variant?: 'plain'
  children: ReactNode
}

export function FormCard({
  onSubmit,
  onChange,
  variant,
  children,
}: FormCardProps) {
  return (
    <form
      className={variant === 'plain' ? styles.plain : styles.form}
      onSubmit={onSubmit}
      onChange={onChange}
      noValidate
    >
      {children}
    </form>
  )
}
