import { useId, type SelectHTMLAttributes } from 'react'
import styles from './TextField.module.css'

type SelectFieldProps = SelectHTMLAttributes<HTMLSelectElement> & {
  label: string
  error?: string
}

export function SelectField({ label, error, ...select }: SelectFieldProps) {
  const id = useId()
  const messageId = `${id}-message`

  return (
    <div className={styles.field}>
      <label className={styles.label} htmlFor={id}>
        {label}
      </label>
      <select
        {...select}
        id={id}
        className={styles.input}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? messageId : undefined}
      />
      {error && (
        <p id={messageId} className={styles.error}>
          {error}
        </p>
      )}
    </div>
  )
}
