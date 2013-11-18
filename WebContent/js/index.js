
$(document).ready(function() {
	autoCompleteTitle();
	$( "#filters_group" ).buttonset();

		
	$("#film").position({
		my : "top+50",
		at : "center",
		of : "#page"
	});
	
	$("#search").position({
		my : "right",
		at : "right",
		of : "#film"
	});	
		
	$("#filters_group").position({
		my : "center",
		at : "top",
		of : "#page"
	});	
	
});

function autoCompleteTitle() {
	$("#film").keypress(function() {
		$("#film").autocomplete({
			source : function(request, response) {
				$.ajax({
					url : "getFilms.jsp",
					data : {
						film : $("#film").val()
					},
					type : "POST",
					dataType : "json",
					success : function(data) {
						response($.map(data.titulos, function(item) {
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
