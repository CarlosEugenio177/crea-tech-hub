import type { InputHTMLAttributes } from 'react'
import { Field } from './Field.tsx'

type TextFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string
  hint?: string
  error?: string
}

export function TextField({ label, hint, error, ...input }: TextFieldProps) {
  return (
    <Field label={label} hint={hint} error={error}>
      {(control) => <input {...input} {...control} />}
    </Field>
  )
}
