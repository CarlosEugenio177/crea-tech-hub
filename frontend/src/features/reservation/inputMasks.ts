import type { UseFormRegisterReturn } from 'react-hook-form'

function groupDigits(value: string, sizes: number[], separator: string) {
  let digits = value.replace(/\D/g, '')
  const groups: string[] = []
  for (const size of sizes) {
    if (digits === '') break
    groups.push(digits.slice(0, size))
    digits = digits.slice(size)
  }
  return groups.join(separator)
}

export function maskDate(value: string) {
  return groupDigits(value, [2, 2, 4], '/')
}

export function withMask<Name extends string>(
  registration: UseFormRegisterReturn<Name>,
  mask: (value: string) => string,
): UseFormRegisterReturn<Name> {
  return {
    ...registration,
    onChange: (event) => {
      const input = event.target as HTMLInputElement
      input.value = mask(input.value)
      return registration.onChange(event)
    },
  }
}
