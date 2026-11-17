import { useId, type ReactNode } from 'react'
import styles from './Field.module.css'

export type FieldControlProps = {
  id: string
  className: string
  'aria-invalid'?: true
  'aria-describedby'?: string
}

type FieldProps = {
  label: string
  hint?: string
  error?: string
  children: (control: FieldControlProps) => ReactNode
}

export function Field({ label, hint, error, children }: FieldProps) {
  const id = useId()
  const messageId = `${id}-message`
  const message = error ?? hint

  return (
    <div className={styles.field}>
      <label className={styles.label} htmlFor={id}>
        {label}
      </label>
      {children({
        id,
        className: styles.input,
        'aria-invalid': error ? true : undefined,
        'aria-describedby': message ? messageId : undefined,
      })}
      {message && (
        <p id={messageId} className={error ? styles.error : styles.hint}>
          {message}
        </p>
      )}
    </div>
  )
}
