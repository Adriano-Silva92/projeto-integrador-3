function atualizarCards() {
    fetch('/admin/dashboard-data')
        .then(response => response.json())
        .then(data => {
            document.getElementById('totalAlunos').innerText = data.totalAlunos;
            document.getElementById('totalUsuarios').innerText = data.totalUsuarios;
            document.getElementById('totalNotas').innerText = data.totalNotas;
        })
        .catch(error => console.error('Erro ao atualizar cards:', error));
}