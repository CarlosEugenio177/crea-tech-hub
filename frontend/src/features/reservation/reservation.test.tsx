import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, test } from 'vitest'
import { renderApp } from '../../test/renderApp.tsx'

async function chooseSpaceAndPeriod(user: ReturnType<typeof userEvent.setup>) {
  await user.click(
    screen.getByRole('button', { name: 'Escolher Sala de podcast' }),
  )
  await user.type(screen.getByLabelText('Data'), '2099-03-10')
  await user.type(screen.getByLabelText('Início'), '09:15')
  await user.type(screen.getByLabelText('Fim'), '10:45')
  await user.click(screen.getByRole('button', { name: 'Continuar' }))
  await screen.findByRole('heading', { name: 'Seus dados' })
}

test('walks from the home page to the review without sending the request', async () => {
  const user = userEvent.setup()
  renderApp('/')

  await user.click(screen.getByRole('link', { name: 'Solicitar um espaço' }))
  await chooseSpaceAndPeriod(user)
  await user.selectOptions(
    screen.getByLabelText('Perfil'),
    'Mutualista / profissional do Crea',
  )
  await user.type(screen.getByLabelText('Nome completo'), 'Marina Silva')
  await user.type(screen.getByLabelText('WhatsApp'), '(86) 99999-0000')
  await user.type(screen.getByLabelText('E-mail'), 'marina@example.com')
  await user.click(screen.getByLabelText('Li e aceito as regras de uso do Hub'))
  await user.click(screen.getByRole('button', { name: 'Revisar solicitação' }))

  expect(
    await screen.findByRole('heading', { name: 'Revise sua solicitação' }),
  ).toBeInTheDocument()
  expect(screen.getByText('Sala de podcast')).toBeInTheDocument()
  expect(screen.getByText('09:15 às 10:45')).toBeInTheDocument()
  expect(screen.getByText('Marina Silva')).toBeInTheDocument()
  expect(
    screen.getByRole('button', { name: 'Enviar solicitação para análise' }),
  ).toBeDisabled()
})

test('requires the reason from the external public and the acceptance of the rules', async () => {
  const user = userEvent.setup()
  renderApp('/reservar')

  await chooseSpaceAndPeriod(user)
  await user.selectOptions(screen.getByLabelText('Perfil'), 'Público externo')
  await user.type(screen.getByLabelText('Nome completo'), 'Lucas Azevedo')
  await user.type(screen.getByLabelText('WhatsApp'), '86999990000')
  await user.type(screen.getByLabelText('E-mail'), 'lucas@example.com')
  await user.click(screen.getByRole('button', { name: 'Revisar solicitação' }))

  expect(
    await screen.findByText('Informe o motivo da reserva.'),
  ).toBeInTheDocument()
  expect(
    screen.getByText('Aceite as regras de uso para continuar.'),
  ).toBeInTheDocument()
  expect(
    screen.getByRole('heading', { name: 'Seus dados' }),
  ).toBeInTheDocument()
})

test('rejects a period that ends before it starts', async () => {
  const user = userEvent.setup()
  renderApp('/reservar')

  await user.click(
    screen.getByRole('button', { name: 'Escolher Estação individual' }),
  )
  await user.type(screen.getByLabelText('Data'), '2099-03-10')
  await user.type(screen.getByLabelText('Início'), '10:00')
  await user.type(screen.getByLabelText('Fim'), '09:00')
  await user.click(screen.getByRole('button', { name: 'Continuar' }))

  expect(
    await screen.findByText('O fim deve ser depois do início.'),
  ).toBeInTheDocument()
})

test('sends visitors who open a later step directly back to the first one', () => {
  renderApp('/reservar/revisao')

  expect(
    screen.getByRole('heading', {
      name: 'Qual espaço acompanha seu próximo projeto?',
    }),
  ).toBeInTheDocument()
})
