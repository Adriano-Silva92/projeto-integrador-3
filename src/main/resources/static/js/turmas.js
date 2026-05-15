const turmaId = document.body.dataset.turmaId;

/*<![CDATA[*/
$(document).ready(function () {

    
    // PESQUISA DE TURMAS
    const searchInput = $('#searchInput');
    const noResultsMsg = $('#noResultsMessage');

    function filterTurmas() {

        const searchTerm = searchInput.val().toLowerCase().trim();

        let visibleCount = 0;

        $('#turmasTableBody tr').each(function () {

            const row = $(this);

            // IGNORA LINHA VAZIA
            if (row.find('td[colspan]').length > 0) {
                return;
            }

            const turmaNome = row.attr('data-nome');

            if (
                turmaNome &&
                turmaNome.toLowerCase().includes(searchTerm)
            ) {
                row.show();
                visibleCount++;
            } else {
                row.hide();
            }

        });

        // MENSAGEM SEM RESULTADOS
        if (visibleCount === 0) {
            noResultsMsg.show();
        } else {
            noResultsMsg.hide();
        }

    }

    let searchTimeout;

    searchInput.on('input', function () {

        clearTimeout(searchTimeout);

        searchTimeout = setTimeout(filterTurmas, 300);

    });

    // EXCLUIR TURMA
    $('.btn-excluir').on('click', function (e) {

        e.preventDefault();

        const turmaId = $(this).data('id');
        const turmaNome = $(this).data('nome');

        const deleteButton = $(this);

        Swal.fire({
            title: '⚠️ Confirmar Exclusão',

            html: `
                <div style="text-align:left;">
                    <p>
                        Você está prestes a excluir a turma:
                    </p>

                    <div style="
                        background:#f8f9fa;
                        padding:15px;
                        border-radius:10px;
                        margin:15px 0;
                    ">
                        <strong style="
                            font-size:18px;
                            color:#dc3545;
                        ">
                            "${turmaNome}"
                        </strong>
                    </div>

                    <p style="font-size:14px;color:#6c757d;">
                        Esta ação removerá alunos vinculados,
                        avaliações e dados relacionados.
                    </p>
                </div>
            `,

            icon: 'warning',

            showCancelButton: true,

            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#6c757d',

            confirmButtonText: 'Sim, excluir',
            cancelButtonText: 'Cancelar',

            reverseButtons: true

        }).then((result) => {

            if (result.isConfirmed) {

                // LOADING 
                Swal.fire({
                    title: 'Excluindo turma...',
                    html: `
                        <div class="spinner-border text-danger"></div>
                        <p class="mt-3">
                            Aguarde...
                        </p>
                    `,
                    allowOutsideClick: false,
                    allowEscapeKey: false,
                    showConfirmButton: false
                });

                // FETCH
                fetch('/turmas/excluir/' + turmaId, {
                    method: 'POST',

                    credentials: 'same-origin',

                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                })

                .then(response => {

                    if (!response.ok) {
                        throw new Error('Erro ao excluir turma');
                    }

                    return response.json();

                })

                .then(data => {

                    Swal.fire({
                        title: '✓ Excluído com sucesso',

                        html: `
                            <div class="text-center">

                                <i class="fas fa-check-circle"
                                   style="
                                       font-size:60px;
                                       color:#28a745;
                                       margin-bottom:15px;
                                   ">
                                </i>

                                <p>
                                    A turma
                                    <strong>"${turmaNome}"</strong>
                                    foi removida.
                                </p>

                            </div>
                        `,

                        icon: 'success',

                        confirmButtonColor: '#28a745',

                        timer: 2500,
                        timerProgressBar: true

                    }).then(() => {

                        // REMOVE A LINHA 
                        deleteButton.closest('tr').fadeOut(300, function () {

                            $(this).remove();

                            // VERIFICA SE AINDA EXISTE TURMAS
                            const remainingRows =
                                $('#turmasTableBody tr:visible').length;

                            if (remainingRows === 0) {

                                location.reload();

                            } else {

                                $('#totalCount').html(`
                                    <i class="fas fa-chalkboard mr-2"></i>
                                    ${remainingRows} turmas no total
                                `);

                            }

                        });

                    });

                })

                .catch(error => {

                    console.error('Erro:', error);

                    Swal.fire({
                        title: '❌ Erro ao excluir',

                        html: `
                            <div class="text-center">

                                <i class="fas fa-exclamation-circle"
                                   style="
                                       font-size:60px;
                                       color:#dc3545;
                                       margin-bottom:15px;
                                   ">
                                </i>

                                <p>
                                    Não foi possível excluir
                                    a turma
                                    <strong>"${turmaNome}"</strong>.
                                </p>

                            </div>
                        `,

                        icon: 'error',

                        confirmButtonColor: '#dc3545'
                    });

                });

            }

        });

    });

    // TOOLTIPS

    $('.btn-entrar').tooltip({
        title: 'Entrar na turma',
        placement: 'top'
    });

    $('.btn-excluir').tooltip({
        title: 'Excluir turma',
        placement: 'top'
    });

    
    // ANIMAÇÃO

    $('#turmasTableBody tr').each(function (index) {

        $(this)
            .css('animation-delay', (index * 0.05) + 's')
            .addClass('animate__animated animate__fadeInUp');

    });

});
/*]]>*/