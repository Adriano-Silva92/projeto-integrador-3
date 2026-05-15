function mostrarAlerta(mensagem, tipo) {
    const container = document.getElementById('alertContainer');

    if (!container) return;

    const alerta = document.createElement('div');
    alerta.className = `alert alert-${tipo} alert-dismissible fade show shadow`;
    alerta.role = 'alert';

    alerta.innerHTML = `
        ${mensagem}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    container.appendChild(alerta);

    
    setTimeout(() => {
        alerta.classList.remove('show');
        alerta.classList.add('fade');

        setTimeout(() => alerta.remove(), 300);
    }, 3000);
}