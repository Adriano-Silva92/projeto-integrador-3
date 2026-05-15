document.getElementById('formAluno').addEventListener('submit', function(e) {
    e.preventDefault();

    const form = this;
    const formData = new FormData(form);

    fetch('/alunos/salvar-ajax', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {

        // FECHA MODAL
        const modal = bootstrap.Modal.getInstance(document.getElementById('modalAluno'));
        modal.hide();

        // LIMPA FORM
        form.reset();

        // ALERTA
        mostrarAlerta('Aluno salvo com sucesso!', 'success');

        // NOVA LINHA
        const novaLinha = `
            <tr>
                <td>${data.ra}</td>
                <td>${data.nome}</td>
                <td>${data.email}</td>
                <td>0.00</td>
                <td>
                    <a href="#"
                       class="btn btn-danger btn-action btn-sm"
                       data-url="/alunos/excluir/${data.id}"
                       data-nome="${data.nome}"
                       onclick="excluirItem(
                           this.dataset.url,
                           this,
                           'o aluno ' + this.dataset.nome
                       )">
                        <i class="fas fa-trash-alt"></i> Excluir
                    </a>
                </td>
            </tr>
        `;

        document.querySelector('#tabelaAlunos tbody')
            .insertAdjacentHTML('beforeend', novaLinha);

        // ATUALIZA CARDS
        atualizarCards();

    })
    .catch(error => {
        console.error(error);
        mostrarAlerta('Erro ao salvar aluno', 'danger');
    });
});