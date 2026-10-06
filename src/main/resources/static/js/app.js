document.addEventListener('DOMContentLoaded', () => {
    const navLinks = document.querySelectorAll('.menu-link');
    const tabContents = document.querySelectorAll('.tab-content');
    const topbarTitle = document.getElementById('topbar-title');

    const tabTitles = {
        'dashboard': 'Visão Geral do Hospital',
        'pacientes': 'Gerenciamento de Pacientes',
        'profissionais': 'Gerenciamento de Profissionais da Saúde',
        'consultas': 'Agendamento e Controle de Consultas',
        'internacoes': 'Controle de Internações Hospitalares',
        'quartos': 'Gerenciamento de Quartos e Leitos',
        'historico': 'Histórico Médico e Prontuário Consolidado'
    };

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetTab = link.getAttribute('data-tab');

            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');

            tabContents.forEach(content => {
                if (content.id === targetTab) {
                    content.classList.add('active');
                } else {
                    content.classList.remove('active');
                }
            });

            if (topbarTitle && tabTitles[targetTab]) {
                topbarTitle.textContent = tabTitles[targetTab];
            }
        });
    });

    // Mock simples para botões de submissão (Aviso de Sprint 1)
    const mockForms = document.querySelectorAll('form');
    mockForms.forEach(form => {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            alert('Sprint 1: Tela sem funcionalidade (Mockup visual para validação de layout). A integração REST/banco ocorrerá nas próximas sprints.');
        });
    });
});
