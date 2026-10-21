# 📱 Aplicativo de Registro e Acompanhamento de Dieta Diária

Aplicativo Android nativo, **100% offline**, construído com **Kotlin**, **Jetpack Compose** e **Room Database**, seguindo a arquitetura moderna recomendada pelo Google (Clean Architecture + MVVM).

---

## 🎯 Funcionalidades e Regras de Negócio

1. **Funcionamento 100% Offline & Armazenamento Local:**
   - Todos os dados (paciente, planos alimentares, refeições planejadas, refeições avulsas e registros de consumo) são persistidos localmente no dispositivo via **Room Database**.
   - Nenhuma dependência de internet ou serviços em nuvem.

2. **Plano Alimentar Vigente por Período:**
   - Cada plano alimentar possui data de início e término no formato `DD/MM/AAAA` (Exemplo inicial: `15/10/2026` a `14/11/2026`).
   - O app consulta no banco de dados o plano correspondente à data selecionada no cabeçalho:
     ```sql
     SELECT * FROM diet_model WHERE :dataSelecionada BETWEEN startDate AND endDate LIMIT 1
     ```
   - O usuário pode alterar as datas de vigência ou estender o plano (+15 dias, +30 dias) na tela dedicada de **Plano Alimentar** para cobrir o intervalo até a próxima consulta nutricional.

3. **Máscara, Validação e Navegação de Datas:**
   - **Máscara de Data:** Máscara automática no formato `DD/MM/AAAA` em todos os campos de entrada de data.
   - **Menu ao Clicar na Data:** Ao clicar na data no cabeçalho da tela principal, abre uma **nova tela dedicada** contendo os três menus: **Paciente**, **Relatório** e **Plano Alimentar**.
   - **Botão "Voltar para Hoje":** Ao navegar para datas anteriores ou posteriores ao dia atual, aparece um botão temporário posicionado **exatamente entre a data e a seta de avançar (`>`)**. Ao retornar ao dia de hoje, o botão desaparece automaticamente.
   - **Bloqueio de Navegação por Vigência (Regra dos 5 dias):** O app bloqueia avançar ou retroceder mais do que 5 dias para datas sem um Plano Alimentar vigente cadastrado. Caso ultrapasse, exibe um alerta visual: *"Não há plano alimentar vigente para esta data"*.

4. **Registro e Edição de Refeições:**
   - **Capitalização Automática:** Primeira letra maiúscula automaticamente (Sentence capitalization) em todos os campos de texto.
   - **Remoção de Quantidade/Peso:** O campo `quantityWeight` foi completamente removido dos formulários.
   - **Horário Real do Consumo:** Campo para o usuário registrar o horário exato da refeição (com máscara `HH:mm`), exibido nos cards da tela inicial sem modificar o horário planejado original do plano alimentar.
   - **Correção de Refeições Removidas:** Sincronização em tempo real garantindo que exclusões de refeições no plano alimentar sejam refletidas imediatamente na tela inicial.
   - **Refeição Avulsa (Extra):** Botão na tela inicial para adicionar refeições extras que ficam salvas exclusivamente para aquele dia, sem alterar o plano alimentar mestre.

5. **Relatório Completo na Tela e Exportação em PDF:**
   - Visualização estruturada dia a dia diretamente na tela (com barra de rolagem), exibindo refeições planejadas, refeições avulsas, horário previsto vs horário real de consumo e alimento consumido.
   - Botão para download/compartilhamento de relatório em PDF de alta qualidade via `android.graphics.pdf.PdfDocument`.

---

## 📂 Estrutura de Pacotes

```
app/src/main/java/com/dieta/app/
├── DietaApp.kt                          # Application class (Singletons)
├── MainActivity.kt                      # Entry point com Jetpack Compose
├── data/
│   ├── dao/
│   │   ├── PatientDao.kt
│   │   ├── DietModelDao.kt
│   │   ├── MealTemplateDao.kt
│   │   └── DailyLogDao.kt
│   ├── database/
│   │   └── AppDatabase.kt               # RoomDatabase (Versão 2)
│   ├── model/
│   │   ├── Patient.kt                   # Entidade do Paciente
│   │   ├── DietModel.kt                 # Entidade do Plano Alimentar
│   │   ├── MealTemplate.kt              # Entidade da Refeição do Plano
│   │   ├── DailyLog.kt                  # Entidade de Registro Diário / Refeição Avulsa
│   │   └── MealWithStatus.kt            # Modelo para listagem com status
│   └── repository/
│       └── DietRepository.kt            # Repositório com sincronização de refeições
├── pdf/
│   └── PdfReportGenerator.kt            # Exportação nativa em PDF
├── util/
│   └── DateMaskUtils.kt                 # Máscara DD/MM/AAAA e HH:mm
└── ui/
    ├── theme/
    │   ├── Color.kt                     # Cores Dark, Vibrant Blue, White
    │   ├── Type.kt
    │   └── Theme.kt
    ├── components/
    │   ├── DateHeader.kt                # Seletor de data + Botão Hoje entre data e '>'
    │   └── MealCard.kt                  # Card de refeição com horários planejado e real
    ├── dialogs/
    │   ├── MealLogDialog.kt             # Registro de refeição do plano (sem peso, com hora real)
    │   └── ExtraMealDialog.kt           # Registro de refeição avulsa do dia
    ├── screens/
    │   ├── HomeScreen.kt                # Tela inicial com cards e atalhos
    │   ├── MainMenuScreen.kt            # Tela dedicada com os 3 menus principais
    │   ├── PatientScreen.kt             # Tela de cadastro/edição do paciente e IMC
    │   ├── ReportScreen.kt              # Relatório dia a dia na tela + exportar PDF
    │   └── PlanManagementScreen.kt      # Gestão de Planos Alimentares e vigências
    └── viewmodel/
        └── DietViewModel.kt             # ViewModel central com StateFlows
```

---

## 🚀 Como Executar

1. Abra o **Android Studio**.
2. Abra a pasta do projeto `Dieta`.
3. Conecte um aparelho ou abra o emulador Android (API 26+).
4. Clique em **Run 'app'** (`Shift + F10`).
