#include "/contracts/randomfastclick/hongkyun/EOS/contract/randomfastclick/randomfastclick.hpp"

    bool compare (std::pair<name, std::pair<uint64_t, double> > target1,
                    std::pair<name, std::pair<uint64_t, double> > target2){
        if(target1.second.first == target2.second.first){
            return target1.second.second > target2.second.second;
        }else{
            return target1.second.first > target2.second.first;
        }
    }

    void randomfastclick::rankreward(){
        require_auth(get_self());
        asset totalreward;
        asset reward;
        std::map<name, asset> reward_userlist;
        int loop;
 
        auto score = _ranks.get_index<"bygamescore"_n>();
        std::vector<std::pair<name, std::pair<uint64_t,double>>> ranklist;
        for(const auto& rank : score){
            ranklist.push_back(std::pair<name, std::pair<uint64_t, double>>(rank.user.value,std::make_pair(rank.game_score,rank.play_time)));
        }

        sort(ranklist.begin(),ranklist.end(),compare);

        if(ranklist.size() >= 3 ? loop = 3 : loop = ranklist.size())

        for(int rewardeos = 0; rewardeos<loop; rewardeos++){    

            auto status_index = _rankgameinfos.get_index<"bystatus"_n>();
            for(const auto& item : status_index){
            if(item.status == "wait"_n){

                totalreward = item.reward;

                switch (rewardeos+1)
                {
                    case 1:
                        reward = totalreward / 2;
                        break;
                    case 2:
                        reward = totalreward / 3;
                        break;
                    case 3:
                        reward = totalreward / 5;
                        break;
                }  
                            
                action(
                    permission_level{get_self(), "active"_n},
                    "eosio.token"_n,
                    "transfer"_n,
                    std::make_tuple(get_self(), ranklist[rewardeos].first, reward, std::string("Congratulations! from Random1to50"))       
                ).send(); 
            }
            }
        }          
    }

     void randomfastclick::pushrankinfo(std::string start_time, std::string end_time, asset reward, asset bet_asset, name status, uint64_t limit){
         require_auth(get_self());

         check(start_time.size() != 11 || end_time.size() != 11 , "timelength is 10!");
         check(status != "wait"_n || status != "close"_n , "status is wait or close!");

        if(limit == 100){
            _rankgameinfos.emplace(get_self(), [&]( auto& rankinfo ){
                rankinfo.key = _rankgameinfos.available_primary_key();
                //start_time & end_time must be either as 1906282200  ex) Year19 Month06 Date28 Hours22 Minutes00 => 1906282200 
                rankinfo.start_time = start_time; 
                rankinfo.end_time = end_time;
                rankinfo.reward = reward;
                rankinfo.bet_asset = bet_asset;
                //status must be either wait or close
                rankinfo.status = status;
            });
        }else{
            auto iterator = _rankgameinfos.find( limit );
            if( iterator != _rankgameinfos.end() ){
                _rankgameinfos.modify(iterator, get_self(), [&]( auto& rankinfo ){
                    //status must be either wait or close
                    rankinfo.status = status;
                });
            }
        }
    }

     void randomfastclick::deletedata(std::string table, uint64_t limit){
         require_auth(get_self());

         if(table == "rank"){
            for(auto iterator = _ranks.begin(); iterator != _ranks.end();){
                iterator = _ranks.erase(iterator);
            }
         }else if(table == "ranklog"){
             if(limit == 100){
                 for(auto iterator = _ranklogs.begin(); iterator != _ranklogs.end();){
                     iterator = _ranklogs.erase(iterator);
                 }
             }else{
                 auto iterator = _ranklogs.find( limit );
                 if( iterator != _ranklogs.end() ){
                     iterator = _ranklogs.erase(iterator);
                 }
             }
         }else if(table == "rankgameinfo"){
             if(limit == 100){
                 for(auto iterator = _rankgameinfos.begin(); iterator != _rankgameinfos.end();){
                     iterator = _rankgameinfos.erase(iterator);
                 }
             }else{
                 auto iterator = _rankgameinfos.find( limit );
                 if( iterator != _rankgameinfos.end() ){
                     iterator = _rankgameinfos.erase(iterator);
                 }
             }
         }
     }

    void randomfastclick::pushresult(name user, bool timeout, bool forcequit, bool touchlistcheck, uint64_t game_score, double play_time, uint64_t start_time, uint64_t end_time){
        require_auth(get_self());

        if(timeout == true || forcequit == true || touchlistcheck == true){}
        else{
            
            auto iterator = _ranks.find( user.value );
            if( iterator == _ranks.end() ){
                _ranks.emplace(get_self(), [&]( auto& order ){
                order.user = user; 
                order.game_score = game_score;
                order.play_time = play_time;
                order.start_time = start_time;
                order.end_time = end_time;
                });
            }else{
                _ranks.modify(iterator, get_self(), [&]( auto& order ){
                order.game_score = game_score;
                order.play_time = play_time;
                order.start_time = start_time;
                order.end_time = end_time;
                });
            }
        };
            _ranklogs.emplace(get_self(), [&]( auto& logorder ){
                logorder.key = _ranklogs.available_primary_key();
                logorder.user = user;
                logorder.timeout = timeout;
                logorder.forcequit = forcequit;
                logorder.touchlistcheck = touchlistcheck;
                logorder.game_score = game_score;
                logorder.play_time = play_time;
                logorder.start_time = start_time;
                logorder.end_time = end_time;
            });
        }

EOSIO_DISPATCH( randomfastclick, (rankreward)(pushrankinfo)(deletedata)(pushresult))