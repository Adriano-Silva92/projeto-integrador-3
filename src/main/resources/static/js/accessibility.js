document.addEventListener('DOMContentLoaded', function () {

    // Aplicar tema salvo para todas as páginas
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
        document.body.classList.remove('dark-theme', 'high-contrast');

        if (savedTheme === 'dark') {
            document.body.classList.add('dark-theme');
        } else if (savedTheme === 'high-contrast') {
            document.body.classList.add('high-contrast');
        }

        // Atualiza botão ativo (se existir na página)
        document.querySelectorAll('.theme-btn').forEach(btn => {
            btn.classList.remove('active');

            if (btn.getAttribute('onclick')?.includes(savedTheme)) {
                btn.classList.add('active');
            }
        });
    }

    // Aplicar preferencias de tamanho da fonte
    const savedFontSize = localStorage.getItem('fontSize');
    if (savedFontSize) {
        document.body.style.fontSize = savedFontSize + '%';
        currentFontSize = parseInt(savedFontSize);
    }

});