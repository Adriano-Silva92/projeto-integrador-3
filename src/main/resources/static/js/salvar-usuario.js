document.getElementById('formUsuario').addEventListener('submit', function(e) {
    e.preventDefault();

    const form = this;
    const formData = new FormData(form);

    fetch('/usuarios/salvar-ajax', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {

        // FECHA MODAL
        const modal = bootstrap.Modal.getInstance(document.getElementById('modalUsuario'));
        modal.hide();

        // LIMPA FORM
        form.reset();

        // ALERTA
        mostrarAlerta('Usuário salvo com sucesso!', 'success');

        // ADICIONA NOVA LINHA
        const novaLinha = `
            <tr>
                <td>${data.nome}</td>
                <td>${data.email}</td>
                <td>
                    <span class="badge-role ${data.role}">
                        ${data.role}
                    </span>
                </td>
                <td>
                    <a href="#"
                       class="btn btn-danger btn-action btn-sm"
                       data-url="/usuarios/excluir/${data.id}"
                       data-perfil="${data.role}"
                       data-nome="${data.nome}"
                       onclick="excluirItem(
                           this.dataset.url,
                           this,
                           'o ' + this.dataset.perfil + ' ' + this.dataset.nome
                       )">
                        <i class="fas fa-trash-alt"></i> Excluir
                    </a>
                </td>
            </tr>
        `;

        document.querySelector('#tabelaUsuarios tbody')
            .insertAdjacentHTML('beforeend', novaLinha);

        // ATUALIZA CARDS
        atualizarCards();

    })
    .catch(error => {
        console.error(error);
        mostrarAlerta('Erro ao salvar usuário', 'danger');
    });
});