document.getElementById('formNota').addEventListener('submit', function(e) {
    e.preventDefault();

    const form = this;
    const formData = new FormData(form);

    fetch('/notas/salvar-ajax', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {

        // FECHA MODAL
        const modal = bootstrap.Modal.getInstance(document.getElementById('modalNota'));
        modal.hide();

        // LIMPA FORM
        form.reset();

        // ALERTA 
        mostrarAlerta('Nota salva com sucesso!', 'success');

        // ADICIONA A NOVA LINHA
        const novaLinha = `
            <tr>
                <td>${data.nome}</td>
                <td>${data.ra}</td>
                <td>${data.disciplina}</td>
                <td>
                    <span class="badge" style="background: linear-gradient(135deg, #667eea, #764ba2); padding: 5px 12px;">
                        ${data.valor}
                    </span>
                </td>
                <td>
                    <a href="#"
                       class="btn btn-danger btn-action btn-sm"
                       data-url="/notas/excluir/${data.id}"
                       data-nome="${data.nome}"
                       data-disciplina="${data.disciplina}"
                       data-valor="${data.valor}"
                       onclick="excluirItem(
                           this.dataset.url,
                           this,
                           'a nota de ' + this.dataset.nome +
                           ' (' + this.dataset.disciplina +
                           ' - Nota: ' + this.dataset.valor + ')'
                       )">
                        <i class="fas fa-trash-alt"></i> Excluir
                    </a>
                </td>
            </tr>
        `;

        document.querySelector('#tabelaNotas tbody')
            .insertAdjacentHTML('beforeend', novaLinha);

        // ATUALIZA CARDS
        atualizarCards();

    })
    .catch(error => {
        console.error(error);
        mostrarAlerta('Erro ao salvar nota', 'danger');
    });
});