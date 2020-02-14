port module Main exposing (Model, Msg(..), inbound, init, main, outbound, update, view)

import Browser
import Browser.Dom as Dom
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode
import Json.Encode
import List.Extra
import Task


type alias Message =
    { id : String
    , messageRegion : String
    , message : String
    , ts : Int
    , source : String
    , envelopeRegion : String
    }


toMessage : String -> EnvelopeMessage -> Message
toMessage envelopeRegion envelopeMessage =
    Message
        envelopeMessage.id
        envelopeMessage.region
        envelopeMessage.message
        envelopeMessage.ts
        envelopeMessage.source
        envelopeRegion


type alias Envelope =
    { messages: List EnvelopeMessage
    , region: String
    }


type alias EnvelopeMessage =
    { id : String
    , region : String
    , message : String
    , ts : Int
    , source : String
    }


type alias Model =
    { error : Maybe String
    , username : Maybe String
    , input : Maybe String
    , messages : List Message
    }


init : ( Model, Cmd Msg )
init =
    ( Model Nothing Nothing Nothing []
    , Http.get
        { url = "https://api.resilient-demo.symphonia.io/history"
        , expect = Http.expectJson LoadHistory envelopeDecoder
        }
    )


-- UPDATE


type Msg
    = NoOp
    | LoadHistory (Result Http.Error Envelope)
    | UpdateInput String
    | SendMessage
    | WebSocketIn Json.Encode.Value


update : Msg -> Model -> ( Model, Cmd Msg )
update message model =
    case message of
        NoOp ->
            ( model, Cmd.none )

        WebSocketIn json ->
            let
                decodedEnvelope =
                    Json.Decode.decodeValue envelopeDecoder json
            in
            case decodedEnvelope of
                Result.Ok envelope ->
                    let
                        newMessages =
                            List.map (toMessage envelope.region) envelope.messages
                    in
                    ( { model | messages = List.append model.messages newMessages |> List.Extra.uniqueBy .id }
                    , jumpToBottom "content"
                    )

                Result.Err err ->
                    ( { model | error = Just (Json.Decode.errorToString err) }, Cmd.none )

        LoadHistory (Result.Ok envelope) ->
            let
               messages =
                    List.map (toMessage envelope.region) envelope.messages
            in
            ( { model | messages = messages, error = Just "Loaded history" }, jumpToBottom "content" )

        LoadHistory (Result.Err _) ->
            ( { model | error = Just "HTTP error" }, Cmd.none )

        UpdateInput input ->
            ( { model | input = Just input }, Cmd.none )

        SendMessage ->
            case model.input of
                Just input ->
                    ( { model | input = Nothing }
                    , messageEncoder input |> outbound
                    )

                Nothing ->
                    ( model, Cmd.none )


envelopeMessageDecoder : Json.Decode.Decoder EnvelopeMessage
envelopeMessageDecoder =
    Json.Decode.map5 EnvelopeMessage
        (Json.Decode.field "id" Json.Decode.string)
        (Json.Decode.field "region" Json.Decode.string)
        (Json.Decode.field "message" Json.Decode.string)
        (Json.Decode.field "ts" Json.Decode.int)
        (Json.Decode.field "source" Json.Decode.string)

envelopeDecoder : Json.Decode.Decoder Envelope
envelopeDecoder =
    Json.Decode.map2 Envelope
        (Json.Decode.field "messages" (Json.Decode.list envelopeMessageDecoder))
        (Json.Decode.field "region" Json.Decode.string)

messageEncoder : String -> Json.Encode.Value
messageEncoder s =
    Json.Encode.object
        [ ( "message", Json.Encode.string s )
        , ( "action", Json.Encode.string "send" )
        ]


jumpToBottom : String -> Cmd Msg
jumpToBottom id =
    Dom.getViewportOf id
        |> Task.andThen (\info -> Dom.setViewportOf id 0 info.scene.height)
        |> Task.attempt (\_ -> NoOp)


view : Model -> Html Msg
view model =
    div [ class "container" ]
        [ div [ class "pure-g" ]
            [ div [ class "pure-u pure-u-1" ]
                [ div [ class "pure-u pure-u-3-5" ] [ b [ ] [ text "Message" ] ]
                --, div [ class "pure-u pure-u-1-5" ] [ b [ ] [ text "Source" ] ]
                , div [ class "pure-u pure-u-1-5" ] [ b [ ] [ text "Original Region" ] ]
                , div [ class "pure-u pure-u-1-5" ] [ b [ ] [ text "Current Region" ] ]
                ]
            , div [ id "content", class "pure-u content" ]
                (List.sortBy .ts model.messages |> List.map viewMessage)
            , div [ class "pure-u pure-u-1 controls" ]
                [ Html.form [ class "pure-form pure-u-1", onSubmit SendMessage ]
                    [ input [ class "pure-input-1", type_ "text", placeholder "What's the good word?", value (Maybe.withDefault "" model.input), onInput UpdateInput ] []
                    ]
                ]
            ]
            , p [] [ text (Maybe.withDefault "" model.error) ]
        ]


viewMessage : Message -> Html Msg
viewMessage message =
    div [ ]
        [ div [ class "pure-u pure-u-3-5" ] [ p [ class "message" ] [ text message.message ] ]
        --, div [ class "pure-u pure-u-1-5" ] [ p [ class "message" ] [ text message.source ] ]
        , div [ class "pure-u pure-u-1-5" ] [ p [ class "message" ] [ text message.messageRegion ] ]
        , div [ class "pure-u pure-u-1-5" ] [ p [ class "message" ] [ text message.envelopeRegion ] ]
        ]



-- MAIN


port outbound : Json.Encode.Value -> Cmd msg


port inbound : (Json.Encode.Value -> msg) -> Sub msg


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.batch
        [ inbound WebSocketIn
        ]


main : Program () Model Msg
main =
    Browser.document
        { init = \_ -> init
        , view =
            \m ->
                { title = ""
                , body = [ view m ]
                }
        , update = update
        , subscriptions = subscriptions
        }
