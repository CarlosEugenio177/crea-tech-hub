import { Navigate, useNavigate } from 'react-router'
import { PeriodForm } from './PeriodForm.tsx'
import { useReservationDraft } from './reservationDraft.ts'
import { RESERVE, RESERVE_REQUESTER } from './reservationRoutes.ts'
import { findSpaceOption } from './spaceOptions.ts'
import { StepPage } from './StepPage.tsx'

export function ChooseTimePage() {
  const navigate = useNavigate()
  const { draft, update } = useReservationDraft()
  const space = findSpaceOption(draft.spaceId)

  if (!space) return <Navigate to={RESERVE} replace />

  return (
    <StepPage
      step={2}
      title="Escolha uma data e um horário"
      description={`${space.name}. Informe início e fim com precisão de minutos.`}
    >
      <PeriodForm
        period={draft.period}
        onSubmit={(period) => {
          update({ period })
          void navigate(RESERVE_REQUESTER)
        }}
      />
    </StepPage>
  )
}
