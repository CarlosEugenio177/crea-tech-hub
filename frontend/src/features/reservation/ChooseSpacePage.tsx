import { useNavigate } from 'react-router'
import styles from './ChooseSpacePage.module.css'
import { useReservationDraft } from './reservationDraft.ts'
import { RESERVE_TIME } from './reservationRoutes.ts'
import { SpaceOptionCard } from './SpaceOptionCard.tsx'
import { SPACE_OPTIONS } from './spaceOptions.ts'
import { StepPage } from './StepPage.tsx'

export function ChooseSpacePage() {
  const navigate = useNavigate()
  const { update } = useReservationDraft()

  return (
    <StepPage
      step={1}
      title="Qual espaço acompanha seu próximo projeto?"
      description="Escolha o ambiente e, em seguida, a data e o horário."
    >
      <ul className={styles.options}>
        {SPACE_OPTIONS.map((option) => (
          <SpaceOptionCard
            key={option.id}
            option={option}
            onChoose={() => {
              update({ spaceId: option.id })
              void navigate(RESERVE_TIME)
            }}
          />
        ))}
      </ul>
    </StepPage>
  )
}
