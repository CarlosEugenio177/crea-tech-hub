import { useState } from 'react'
import { useNavigate } from 'react-router'
import { RecommendationForm } from './RecommendationForm.tsx'
import { RecommendationResults } from './RecommendationResults.tsx'
import {
  useReservationDraft,
  type RecommendationCriteria,
} from './reservationDraft.ts'
import { RESERVE_TIME } from './reservationRoutes.ts'
import { StepPage } from './StepPage.tsx'

export function RecommendationPage() {
  const navigate = useNavigate()
  const { draft, update } = useReservationDraft()
  const [criteria, setCriteria] = useState<RecommendationCriteria | undefined>(
    draft.recommendation,
  )

  return (
    <StepPage
      step={1}
      title="Encontre um espaço"
      description="Informe quantas pessoas vão participar e quando."
    >
      <RecommendationForm
        initial={draft.recommendation}
        onEdit={() => setCriteria(undefined)}
        onSubmit={setCriteria}
      />
      {criteria && (
        <RecommendationResults
          criteria={criteria}
          onChoose={(space) => {
            update({
              space: { id: space.id, name: space.name },
              period: undefined,
              station: undefined,
              recommendation: criteria,
            })
            void navigate(RESERVE_TIME)
          }}
        />
      )}
    </StepPage>
  )
}
