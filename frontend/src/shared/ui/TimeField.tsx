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
  options?: readonly string[]
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
  options,
}: TimeFieldProps) {
  const id = useId()
  const [hour, minute = '00'] =
    value && (!options || options.includes(value)) ? value.split(':') : ['']
  const allowedHours = options
    ? [...new Set(options.map((time) => time.slice(0, 2)))]
    : hours
  const minutesFor = (selectedHour: string) =>
    options
      ? options
          .filter((time) => time.startsWith(selectedHour + ':'))
          .map((time) => time.slice(3))
      : minutes
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
          disabled={allowedHours.length === 0}
          onBlur={onBlur}
          onChange={(event) => {
            const selected = event.currentTarget.value
            const allowedMinutes = minutesFor(selected)
            const nextMinute = allowedMinutes.includes(minute)
              ? minute
              : allowedMinutes[0]
            onChange(selected ? `${selected}:${nextMinute}` : '')
          }}
        >
          <option value="">Hora</option>
          {allowedHours.map((time) => (
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
          {(hour ? minutesFor(hour) : ['00']).map((time) => (
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
