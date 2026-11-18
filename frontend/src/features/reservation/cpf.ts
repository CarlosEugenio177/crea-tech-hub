export function cpfDigits(value: string) {
  return value.replace(/\D/g, '')
}

export function isValidCpf(value: string) {
  const digits = cpfDigits(value)
  if (!/^\d{11}$/.test(digits) || /^(\d)\1{10}$/.test(digits)) return false
  return [9, 10].every((count) => {
    const sum = [...digits.slice(0, count)].reduce(
      (total, digit, index) => total + Number(digit) * (count + 1 - index),
      0,
    )
    return ((sum * 10) % 11) % 10 === Number(digits[count])
  })
}
