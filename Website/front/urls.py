from django.urls import path

from . import views

urlpatterns = [
    path('', views.main_page, name='main page'),
    path('get/ajax/list', views.get_list, name="get_list"),

    # path('', views.main_page, name='main page'),
    # path('get/ajax/lyrics', views.get_lyrics, name="get_lyrics"),
    # path('get/ajax/parody_lyrics', views.get_parody, name="get_parody")
]