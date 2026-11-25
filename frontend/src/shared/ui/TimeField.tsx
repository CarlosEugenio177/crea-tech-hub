import { useId, type Ref } from 'react'
import fieldStyles from './Field.module.css'
import styles from './TimeField.module.css'

const timeParts = (count: number) =>
  Array.from({ length: count }, (_, index) => String(index).padStart(2, '0'))
const hours = timeParts(24)
const minutes = timeParts(60)

export type TimeFieldControl = {
  value?: string
  onChange: (value: string) => void
  onBlur?: () => void
  ref?: Ref<HTMLSelectElement>
}

type TimeFieldProps = TimeFieldControl & {
  label: string
  error?: string
}

export function TimeField({
  label,
  error,
  value,
  onChange,
  onBlur,
  ref,
}: TimeFieldProps) {
  const id = useId()
  const [hour, minute = '00'] = value?.split(':') ?? ['']
  const messageId = error ? `${id}-error` : undefined

  return (
    <fieldset className={styles.group}>
      <legend className={fieldStyles.label}>{label}</legend>
      <div className={styles.controls}>
        <select
          ref={ref}
          className={fieldStyles.input}
          aria-label={`${label} — hora`}
          aria-describedby={messageId}
          aria-invalid={error ? true : undefined}
          value={hour}
          onBlur={onBlur}
          onChange={(event) => {
            const selected = event.currentTarget.value
            onChange(selected ? `${selected}:${minute}` : '')
          }}
        >
          <option value="">Hora</option>
          {hours.map((time) => (
            <option key={time}>{time}</option>
          ))}
        </select>
        <span aria-hidden="true">:</span>
        <select
          className={fieldStyles.input}
          aria-label={`${label} — minutos`}
          aria-describedby={messageId}
          aria-invalid={error ? true : undefined}
          value={minute}
          disabled={!hour}
          onBlur={onBlur}
          onChange={(event) => onChange(`${hour}:${event.currentTarget.value}`)}
        >
          {minutes.map((time) => (
            <option key={time}>{time}</option>
          ))}
        </select>
      </div>
      {error && (
        <p id={messageId} className={fieldStyles.error}>
          {error}
        </p>
      )}
    </fieldset>
  )
}
