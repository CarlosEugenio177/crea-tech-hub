import { useId, type TextareaHTMLAttributes } from 'react'
import styles from './TextField.module.css'

type TextAreaFieldProps = TextareaHTMLAttributes<HTMLTextAreaElement> & {
  label: string
  hint?: string
  error?: string
}

export function TextAreaField({
  label,
  hint,
  error,
  ...textarea
}: TextAreaFieldProps) {
  const id = useId()
  const messageId = `${id}-message`
  const message = error ?? hint

  return (
    <div className={styles.field}>
      <label className={styles.label} htmlFor={id}>
        {label}
      </label>
      <textarea
        {...textarea}
        id={id}
        className={styles.input}
        aria-invalid={error ? true : undefined}
        aria-describedby={message ? messageId : undefined}
      />
      {message && (
        <p id={messageId} className={error ? styles.error : styles.hint}>
          {message}
        </p>
      )}
    </div>
  )
}
