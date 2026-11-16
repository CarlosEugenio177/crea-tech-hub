const fullDate = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'full' })

export function today() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

export function formatDate(date: string) {
  return fullDate.format(new Date(`${date}T00:00`))
}
