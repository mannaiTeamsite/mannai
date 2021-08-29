var artl = $('body').hasClass("ar") ? true : false;

var items;
var item = 1;
$('#surveyFormModal').on('shown.bs.modal', function (event) {
    items = $('#surveyQuestions .item').length;
    console.log(items);
});



$(document).on('click', '.s_NextBtn', function () {
    console.log(item)
    item = item+1;
    console.log(items, item)
    $('#surveyQuestions .item').removeClass('active')
    $('#surveyQuestions .item').eq(item-1).addClass('active').find('input, textarea, select').focus();
    if(items == item){
        $('.s_NextBtn').text("Submit").addClass('s_submit');
    }
});

$(document).on('click', '.s_reset', function () {
    item = 1;
    $('#surveyQuestions .item').removeClass('active')
    $('#surveyQuestions .item').eq(item-1).addClass('active');
    $('.s_NextBtn').text("Next").removeClass('s_submit');
    resetSurvey()

});

$('#surveyFormModal').on('hidden.bs.modal', function (event) {
    item = 1;
    $('#surveyQuestions .item').removeClass('active')
    $('#surveyQuestions .item').eq(item-1).addClass('active');
    $('.s_NextBtn').text("Next").removeClass('s_submit');
    $('.options').find('.if_others').removeClass('other_in');
    resetSurvey()
});


function resetSurvey(){
    $('input[type="radio"]').prop("checked", false);
    $('input[type="checkbox"]').prop("checked", false);
    $('input[type="text"]').val("");
    $('textarea').val("");
    $('.s_ques_wrapper .selectpicker').selectpicker('refresh');
}
$(document).on('click', '.s_submit', function () {
    $('#surveySubmitSuccess').modal('show')
})

$(document).on('click', '.radio_wrap input[type="radio"]', function(){
    if($(this).val()=='other' && $(this).is(':checked')){
        $(this).parents('.options').find('.if_others').addClass('other_in')
    }
    else{
        $(this).parents('.options').find('.if_others').removeClass('other_in')
    }
});

$(document).on('change', 'select', function(){
    if($(this).val()=='other'){
        $(this).parents('.s_ques_wrapper').find('.if_others').addClass('other_in')
    }
    else{
        $(this).parents('.s_ques_wrapper').find('.if_others').removeClass('other_in')
    }
})