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
				console.log(item.nome);
				var o = new Option(item.nome, item.sigla);
				$('#estados').append(o);
			});
			var option = $('#estados option').first();
			var estado = $(option).val();
			$.each(data.estados, function(i, item) {
				if (estado == item.sigla) {
					console.log("Cidades: ");
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
					console.log("Cidades: ");
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
		element.addClass("active");
	}

	setDefaultActive();

	carregarEstados();



});