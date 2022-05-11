from django.http import JsonResponse
from django.shortcuts import render

import requests


def get_list(request):
    if request.method == "GET":
        target = request.GET.get("target", None)
        listFilms = []
        if target == "leaderboardBestScore":
            listFilms = [
                {
                    "name": "first",
                    "value": 10
                },
                {
                    "name": "second",
                    "value": 5
                },
                {
                    "name": "third",
                    "value": 1
                },
            ]
            # # TODO get json
            # r = requests.get('')
            # if r.status_code == 200:
            #     data = r.json()
            #     # TODO parse json -> listFilms
            #     for elem in data:
            #         listFilms.append({
            #             "name": elem['title'],
            #             "value": elem['score']
            #         })
            # else:
            #     print(f"Error in request for {target}")
            #     print(r.status_code)
        elif target == "leaderboardWorstScore":
            listFilms = [
                    {
                        "name": "first",
                        "value": 10
                    },
                    {
                        "name": "second",
                        "value": 5
                    },
                    {
                        "name": "third",
                        "value": 1
                    },
                ]
            # # TODO get json
            # r = requests.get('')
            # if r.status_code == 200:
            #     data = r.json()
            #     # TODO parse json -> listFilms
            #     for elem in data:
            #         listFilms.append({
            #             "name": elem['title'],
            #             "value": elem['score']
            #         })
            # else:
            #     print(f"Error in request for {target}")
            #     print(r.status_code)
        elif target == "leaderboardBestView":
            listFilms = [
                    {
                        "name": "first",
                        "value": 10
                    },
                    {
                        "name": "second",
                        "value": 5
                    },
                    {
                        "name": "third",
                        "value": 1
                    },
                ]
            # # TODO get json
            # r = requests.get('')
            # if r.status_code == 200:
            #     data = r.json()
            #     # TODO parse json -> listFilms
            #     for elem in data:
            #         listFilms.append({
            #             "name": elem['title'],
            #             "value": elem['views']
            #         })
            # else:
            #     print(f"Error in request for {target}")
            #     print(r.status_code)
        elif target == "leaderboardWorstView":
            listFilms = [
                    {
                        "name": "first",
                        "value": 10
                    },
                    {
                        "name": "second",
                        "value": 5
                    },
                    {
                        "name": "third",
                        "value": 1
                    },
                ]
            # # TODO get json
            # r = requests.get('')
            # if r.status_code == 200:
            #     data = r.json()
            #     # TODO parse json -> listFilms
            #     for elem in data:
            #         listFilms.append({
            #             "name": elem['title'],
            #             "value": elem['views']
            #         })
            # else:
            #     print(f"Error in request for {target}")
            #     print(r.status_code)
        else:
            return JsonResponse("need the id of the song to get lyrics", status=400)

        return render(request, 'leaderboard.html', {"listFilms": listFilms})

    return JsonResponse({}, status=400)


def main_page(request):
    context = {
        'themes': [],
        'songs': [],
    }
    return render(request, 'main_page.html', context)
