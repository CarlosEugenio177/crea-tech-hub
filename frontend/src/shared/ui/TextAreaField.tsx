import type { TextareaHTMLAttributes } from 'react'
import { Field } from './Field.tsx'

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
  return (
    <Field label={label} hint={hint} error={error}>
      {(control) => <textarea {...textarea} {...control} />}
    </Field>
  )
}
