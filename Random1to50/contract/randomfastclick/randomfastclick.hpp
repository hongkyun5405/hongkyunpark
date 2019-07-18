#pragma once

#include <eosio/eosio.hpp>
#include <eosio/asset.hpp>

using namespace eosio;

class [[eosio::contract]] randomfastclick : public eosio::contract {

    public:
        //using contract::contract;

        randomfastclick( name receiver, name code, datastream<const char*> ds ): contract(receiver, code, ds),_ranks(receiver, code.value), _ranklogs(receiver, code.value), _rankgameinfos(receiver, code.value){}
    
    [[eosio::action]]
    void rankreward();

    [[eosio::action]]
    void deletedata(std::string table, uint64_t limit);   

    [[eosio::action]]
    void pushrankinfo(std::string start_time, std::string end_time, asset reward, asset bet_asset, name status, uint64_t limit);   
        
    [[eosio::action]]
    void pushresult(name user, bool timeout, bool forcequit, bool touchlistcheck, uint64_t game_score, double play_time, uint64_t start_time, uint64_t end_time);

    private:

        struct [[eosio::table]] rank {
            name user; //primary  key, acount name
            uint64_t game_score;
            double play_time;
            uint64_t start_time;
            uint64_t end_time;

            uint64_t primary_key() const { return user.value; }
            uint64_t get_gamescore() const { return game_score; }
            double get_playtime() const { return play_time; }
        };
        typedef multi_index<"rank"_n, rank,
        indexed_by<"bygamescore"_n, const_mem_fun<rank, uint64_t, &rank::get_gamescore>>,
        indexed_by<"byplaytime"_n, const_mem_fun<rank, double, &rank::get_playtime>>> ranks;
        
        struct [[eosio::table]] ranklog {
            uint64_t key; //primary  key
            name user;
            bool timeout;
            bool forcequit;
            bool touchlistcheck;
            uint64_t game_score;
            double play_time;
            uint64_t start_time;
            uint64_t end_time;

            uint64_t primary_key() const { return key; }
        };
        typedef multi_index<"ranklog"_n, ranklog> ranklogs;

        struct [[eosio::table]] rankgameinfo {
            uint64_t key; //primary  key
            std::string start_time;
            std::string end_time;
            asset reward;
            asset bet_asset;
            name status;

            uint64_t primary_key() const { return key; }
            uint64_t get_status() const { return status.value; }
        };
        typedef multi_index<"rankgameinfo"_n, rankgameinfo,
        indexed_by<"bystatus"_n, const_mem_fun<rankgameinfo, uint64_t, &rankgameinfo::get_status>>> rankgameinfos;


    ranks _ranks;
    ranklogs _ranklogs;
    rankgameinfos _rankgameinfos;
};