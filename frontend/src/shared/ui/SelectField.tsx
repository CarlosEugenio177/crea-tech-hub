import type { SelectHTMLAttributes } from 'react'
import { Field } from './Field.tsx'

type SelectFieldProps = SelectHTMLAttributes<HTMLSelectElement> & {
  label: string
  hint?: string
  error?: string
}

export function SelectField({
  label,
  hint,
  error,
  ...select
}: SelectFieldProps) {
  return (
    <Field label={label} hint={hint} error={error}>
      {(control) => <select {...select} {...control} />}
    </Field>
  )
}
