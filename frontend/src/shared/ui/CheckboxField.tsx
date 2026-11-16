import { useId, type InputHTMLAttributes } from 'react'
import styles from './CheckboxField.module.css'

type CheckboxFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string
  error?: string
}

export function CheckboxField({ label, error, ...input }: CheckboxFieldProps) {
  const id = useId()
  const messageId = `${id}-message`

  return (
    <div className={styles.field}>
      <div className={styles.option}>
        <input
          {...input}
          id={id}
          type="checkbox"
          className={styles.input}
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? messageId : undefined}
        />
        <label className={styles.label} htmlFor={id}>
          {label}
        </label>
      </div>
      {error && (
        <p id={messageId} className={styles.error}>
          {error}
        </p>
      )}
    </div>
  )
}
