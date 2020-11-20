$(function() {
	/*
		function carregarEstados() {
			$.getJSON("/json/Estados.json", function(data) {
				$.each(data, function(i, item) {
					var o = new Option(item.Nome, item.ID);
					$('#estados').append(o);
				});
				var option = $('#estados option').first();
				var estado = $(option).val();
				console.log(estado);
				$.getJSON("/json/Cidades.json", function(data) {
					$.each(data, function(i, item) {
						if (estado == item.Estado) {
							console.log(item.Nome + " " + item.Estado);
							var o = new Option(item.Nome, item.Nome);
							$('#cidade').append(o);
	
						}
					});
				});
			});
		}
	
		$("#estados").change(function() {
			var estado = $(this).val();
			console.log(estado)
			$('#cidade').empty();
			$.getJSON("/json/Cidades.json", function(data) {
				$.each(data, function(i, item) {
					if (estado == item.Estado) {
						var o = new Option(item.Nome, item.Nome);
						$('#cidade').append(o);
	
					}
				});
			});
		});
	
		carregarEstados();
		
		*/

	function carregarEstados() {
		$.getJSON("/json/estados-cidades.json", function(data) {
			$.each(data.estados, function(i, item) {
				var o = new Option(item.nome, item.sigla);
				$('#estados').append(o);
			});
			var option = $('#estados option').first();
			var estado = $(option).val();
			$.each(data.estados, function(i, item) {
				if (estado == item.sigla) {
					$.each(item.cidades, function(i, cidades) {
						var o = new Option(cidades, cidades);
						$('#cidade').append(o);
					});
				}
			});
		});
	}

	$('#estados').change(function() {
		var estado = $(this).val();
		$('#cidade').empty();
		$.getJSON("/json/estados-cidades.json", function(data) {
			$.each(data.estados, function(i, item) {
				if (estado == item.sigla) {
					$.each(item.cidades, function(i, cidades) {
						var o = new Option(cidades, cidades);
						$('#cidade').append(o);
					});
				}
			});
		});
	});

	var setDefaultActive = function() {
		var path = window.location.pathname;
		console.log(path);
		var element = $("a.nav-link[href='" + path + "']");
		if(path.startsWith('/edit/') || path.startsWith('/salvar')){
			element = $("a.nav-link[href='/cadastro']");
		}else if(path.startsWith('/sort/')){
			element = $("a.nav-link[href='/pessoas/1']");
		}
		element.addClass("active border-menu");

	}

	setDefaultActive();
	carregarEstados();


	$('#pesquisa').keyup(function() {
		var texto = $(this).val();
		$.ajax({
			type: "GET",
			url: "/pesquisa",
			data: {
				texto: texto,
			},
			success: function(response) {
				$('.table-pessoas tbody').replaceWith(response.tbody);
				$('.table-arquivos tbody').replaceWith(response.tbodyFile);
			}
		});
	});

});