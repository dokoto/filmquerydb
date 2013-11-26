$(document).ready(function() {
	$("#film").attr("disabled", false);
	$("#search").attr("disabled", false);
	autoCompleteTitle();
	$("#panel_filter").buttonset();
	$("#panel_complete_type").buttonset();

	$("#search").click(function() {
		$(this).attr("disabled", true);
		GetResults();
	});

	$('#film').on('keypress', function(event) {
		if (event.which == '13') {
			$("#film").attr("disabled", true);
			$("#search").attr("disabled", true);
			GetResults();

		}
	});

});

function GetResults() {
	$.ajax({
		url : "getFilm.jsp",
		data : {
			film : $("#film").val(),
			filter_query : $(".filters_group:checked").val(),
			filter_rgx : $(".filters_group_type:checked").val()
		},
		type : "POST",
		dataType : "json",
		success : buildResultLines
	});
}

function GetImage(id_name, mongo_id_image) {
	$.ajax({
		url : "getImage.jsp",
		data : {
			image : mongo_id_image
		},
		type : "POST",
		dataType : "json",
		success : function(data) {			
			$("#" + id_name).attr("src", "data:image/jpeg;base64," + data.value);
		}
	});

}

function ImageExist(url) {
	var img = new Image();
	img.src = url;
	return img.height != 0;
}

function StripImageName(name)
{
	var s1 = name.substr(name.indexOf("/")+1);
	var s2 = s1.substr(0, s1.indexOf("."));
	return s2;
}

function buildResultLines(data) {
	$("#panel_result").html("");
	for (var i = 0; i < data.values.length; i++) {
		var image_id = StripImageName(data.values[i].foto_mini);
		$("#panel_result").append(
				'<div id="line" class="shape wapper">' 
						+ '<div class="block1">'
						+ '<img src="img/ico_film.png" id="' + image_id + '" alt="image"/>'
						+ '</div>'
						+ '<div class="block2">'
						+ '<p class="p1">'
						+ data.values[i].titulo
						+ '</p>'
						+ '<p class="p2">'
						+ data.values[i].release_date + '<br />'
						+ data.values[i].generos + '<br />'
						+ data.values[i].directores + '<br />'
						+ data.values[i].actores
						+ '</p>'
						+ '</div>'
						+ '<div class="block3">'
						+ '<p class="p3">'
						+ data.values[i].sinopsis
						+ '</p>'
						+ '</div>'
						+ '</div>');
		GetImage(image_id, data.values[i].foto_mini);
	}
	$("#film").attr("disabled", false);
	$("#search").attr("disabled", false);
}

function autoCompleteTitle() {
	$("#film").keypress(function() {
		$("#film").autocomplete({
			source : function(request, response) {
				$.ajax({
					url : "getFilms.jsp",
					data : {
						film : $("#film").val(),
						filter_query : $(".filters_group:checked").val(),
						filter_rgx : $(".filters_group_type:checked").val()
					},
					type : "POST",
					dataType : "json",
					success : function(data) {
						response($.map(data.values, function(item) {
							return {
								label : item,
								value : item
							};
						}));
					}
				});
			},
			select : function(event, ui) {
				if (ui.item)
					showFilm(ui.item);
			},
			open : function() {
				$(this).removeClass("ui-corner-all").addClass("ui-corner-top");
			},
			close : function() {
				$(this).removeClass("ui-corner-top").addClass("ui-corner-all");
			}
		});
	});
}

function showFilm(item) {

}
